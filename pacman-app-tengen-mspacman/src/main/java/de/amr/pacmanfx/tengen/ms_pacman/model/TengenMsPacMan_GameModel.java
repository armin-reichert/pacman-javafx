/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.ANIM_MS_PAC_MAN_BOOSTER;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends GameModel {

    private static final byte FIRST_LEVEL_NUMBER = 1;
    private static final byte LAST_LEVEL_NUMBER = 32;
    private static final byte DEMO_LEVEL_MIN_DURATION_SEC = 20;

    private static final byte PELLET_VALUE = 10;
    private static final byte ENERGIZER_VALUE = 50;

    // See https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PowerPelletTimes.asm
    // Hex value divided by 16 gives the duration in seconds
    private static final byte[] POWER_PELLET_TIMES = {
        0x60, 0x50, 0x40, 0x30, 0x20, 0x50, 0x20, 0x1C, // levels 1-8
        0x18, 0x40, 0x20, 0x1C, 0x18, 0x20, 0x1C, 0x18, // levels 9-16
        0x00, 0x18, 0x20                                // levels 17, 18, then 19+
    };

    // Bonus symbols in Arcade, Mini and Big mazes
    public static final byte BONUS_CHERRY      = 0;
    public static final byte BONUS_STRAWBERRY  = 1;
    public static final byte BONUS_ORANGE      = 2;
    public static final byte BONUS_PRETZEL     = 3;
    public static final byte BONUS_APPLE       = 4;
    public static final byte BONUS_PEAR        = 5;
    public static final byte BONUS_BANANA      = 6;

    // Additional bonus symbols in Strange mazes
    public static final byte BONUS_MILK        = 7;
    public static final byte BONUS_ICE_CREAM   = 8;
    public static final byte BONUS_HIGH_HEELS  = 9;
    public static final byte BONUS_STAR        = 10;
    public static final byte BONUS_HAND        = 11;
    public static final byte BONUS_RING        = 12;
    public static final byte BONUS_FLOWER      = 13;

    // Bonus value = factor * 100
    private static final byte[] BONUS_VALUE_FACTORS = new byte[14];

    static {
        BONUS_VALUE_FACTORS[BONUS_CHERRY]        = 1;
        BONUS_VALUE_FACTORS[BONUS_STRAWBERRY]    = 2;
        BONUS_VALUE_FACTORS[BONUS_ORANGE]        = 5;
        BONUS_VALUE_FACTORS[BONUS_PRETZEL]       = 7;
        BONUS_VALUE_FACTORS[BONUS_APPLE]         = 10;
        BONUS_VALUE_FACTORS[BONUS_PEAR]          = 20;
        BONUS_VALUE_FACTORS[BONUS_BANANA]        = 50; // !!
        BONUS_VALUE_FACTORS[BONUS_MILK]          = 30; // !!
        BONUS_VALUE_FACTORS[BONUS_ICE_CREAM]     = 40; // !!
        BONUS_VALUE_FACTORS[BONUS_HIGH_HEELS]    = 60;
        BONUS_VALUE_FACTORS[BONUS_STAR]          = 70;
        BONUS_VALUE_FACTORS[BONUS_HAND]          = 80;
        BONUS_VALUE_FACTORS[BONUS_RING]          = 90;
        BONUS_VALUE_FACTORS[BONUS_FLOWER]        = 100;
    }

    private static final byte[] KILLED_GHOST_VALUE_FACTORS = {2, 4, 8, 16}; // points = factor * 100

    private static final byte[][] HOUSE = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$, DOOR.$, WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, WALL_V.$   },
        { WALL_V.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, WALL_V.$   },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    public static Pac createMsPacMan(GameContext gameContext) {
        var msPacMan = new Pac(gameContext, "Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public static Pac createPacMan(GameContext gameContext) {
        var pacMan = new Pac(gameContext, "Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public static Ghost createGhost(GameContext gameContext, byte personality) {
        requireValidGhostPersonality(personality);
        return switch (personality) {

            case RED_GHOST_SHADOW -> new Ghost(gameContext, RED_GHOST_SHADOW, "Blinky") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.theGameLevel();

                    float speed = gameContext.theGame().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    setSpeed(speed);
                    if (gameContext.theGame().huntingTimer().phaseIndex() == 0) {
                        roam();
                    } else {
                        boolean chase = gameContext.theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                        Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                        tryMovingTowardsTargetTile(targetTile);
                    }
                }

                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    return gameContext.theGameLevel().pac().tile();
                }
            };

            case PINK_GHOST_SPEEDY -> new Ghost(gameContext, PINK_GHOST_SPEEDY, "Pinky") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.theGameLevel();

                    float speed = gameContext.theGame().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    setSpeed(speed);
                    if (gameContext.theGame().huntingTimer().phaseIndex() == 0) {
                        roam();
                    } else {
                        boolean chase = gameContext.theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                        Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                        tryMovingTowardsTargetTile(targetTile);
                    }
                }

                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    return gameContext.theGameLevel().pac().tilesAhead(4);
                }
            };

            case CYAN_GHOST_BASHFUL -> new Ghost(gameContext, CYAN_GHOST_BASHFUL, "Inky") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.theGameLevel();

                    float speed = gameContext.theGame().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    boolean chase = gameContext.theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                    setSpeed(speed);
                    tryMovingTowardsTargetTile(targetTile);
                }

                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    GameLevel level = gameContext.theGameLevel();
                    return level.pac().tilesAhead(2).scaled(2).minus(level.ghost(RED_GHOST_SHADOW).tile());
                }
            };

            case ORANGE_GHOST_POKEY -> new Ghost(gameContext, ORANGE_GHOST_POKEY, "Sue") {
                @Override
                public void hunt() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return;
                    GameLevel level = gameContext.theGameLevel();
                    float speed = gameContext.theGame().actorSpeedControl().ghostAttackSpeed(gameContext, level, this);
                    boolean chase = gameContext.theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(personality());
                    setSpeed(speed);
                    tryMovingTowardsTargetTile(targetTile);
                }

                @Override
                public Vector2i chasingTargetTile() {
                    if (gameContext == null || gameContext.optGameLevel().isEmpty()) return null;
                    GameLevel level = gameContext.theGameLevel();
                    return tile().euclideanDist(level.pac().tile()) < 8 ? level.ghostScatterTile(personality()) : level.pac().tile();
                }
            };
            default -> throw new IllegalArgumentException("Illegal ghost personality " + personality);
        };
    }

    private final TengenMsPacMan_HUD hud = new TengenMsPacMan_HUD();
    private final TengenMsPacMan_MapSelector mapSelector;
    private final TengenActorSpeedControl actorSpeedControl;
    private final GateKeeper gateKeeper;
    private final HuntingTimer huntingTimer;
    private final Steering autopilot;
    private final Steering demoLevelSteering;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private PacBooster pacBooster;
    private boolean boosterActive;
    private int startLevelNumber; // 1-7
    private boolean canStartNewGame;
    private int numContinues;

    public TengenMsPacMan_GameModel(GameContext gameContext, File highScoreFile) {
        super(gameContext.theGameEventManager(), highScoreFile);
        actorSpeedControl = new TengenActorSpeedControl();
        mapSelector = new TengenMsPacMan_MapSelector();
        gateKeeper = new GateKeeper(this); //TODO implement Tengen logic instead
        huntingTimer = new TengenMsPacMan_HuntingTimer();
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) {
                level.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::reverseAtNextOccasion);
            }
        });
        autopilot = new RuleBasedPacSteering(this);
        demoLevelSteering = new RuleBasedPacSteering(this);
    }

    @Override
    public TengenMsPacMan_HUD theHUD() {
        return hud;
    }

    public void init() {
        mapSelector.loadAllMaps();
        setInitialLifeCount(3);
        resetEverything();
    }

    @Override
    public void resetEverything() {
        prepareForNewGame();
        setPacBooster(PacBooster.OFF);
        setDifficulty(Difficulty.NORMAL);
        setMapCategory(MapCategory.ARCADE);
        setStartLevelNumber(1);
        numContinues = 4;
    }

    @Override
    public void prepareForNewGame() {
        setLifeCount(initialLifeCount());
        level = null;
        propertyMap().clear();
        hud.theLevelCounter().clear();
        playingProperty().set(false);
        boosterActive = false;
        loadHighScore();
        resetScore();
        gateKeeper.reset();
    }

    @Override
    public void onGameEnding() {
        playingProperty().set(false);
        updateHighScore();
        level.showMessage(GameLevel.MESSAGE_GAME_OVER);
    }

    @Override
    public ActorSpeedControl actorSpeedControl() { return actorSpeedControl; }

    @Override
    public HuntingTimer huntingTimer() { return huntingTimer; }

    @Override
    public Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public MapSelector mapSelector() { return mapSelector; }

    public boolean optionsAreInitial() {
        return pacBooster == PacBooster.OFF
            && difficulty == Difficulty.NORMAL
            && mapCategory == MapCategory.ARCADE
            && startLevelNumber == 1
            && numContinues == 4;
    }

    public void setPacBooster(PacBooster mode) {
        pacBooster = mode;
    }

    public PacBooster pacBooster() {
        return pacBooster;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = requireNonNull(mapCategory);
        if (mapCategory == MapCategory.ARCADE) {
            /* see https://tcrf.net/Ms._Pac-Man_(NES,_Tengen):
            Humorously, instead of adding a check to disable multiple extra lives,
            the "Arcade" maze set sets the remaining 3 extra life scores to over 970,000 points,
            a score normally unachievable without cheat codes, since all maze sets end after 32 stages.
            This was most likely done to simulate the Arcade game only giving one extra life per game.
            */
            setExtraLifeScores(Set.of(10_000, 970_000, 980_000, 990_000));
        } else {
            setExtraLifeScores(Set.of(10_000, 50_000, 100_000, 300_000));
        }
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setStartLevelNumber(int number) {
        if (number < FIRST_LEVEL_NUMBER || number > LAST_LEVEL_NUMBER) {
            throw GameException.invalidLevelNumber(number);
        }
        startLevelNumber = number;
    }

    public int startLevelNumber() {
        return startLevelNumber;
    }

    public int numContinues() {
        return numContinues;
    }

    @Override
    public boolean continueOnGameOver() {
        if (startLevelNumber >= 10 && numContinues > 0) {
            numContinues -= 1;
            return true;
        } else {
            numContinues = 4;
            return false;
        }
    }

    public boolean isBoosterActive() {
        return boosterActive;
    }

    @Override
    public boolean canStartNewGame() {
        return canStartNewGame;
    }

    public void setCanStartNewGame(boolean canStartNewGame) {
        this.canStartNewGame = canStartNewGame;
    }

    @Override
    public void startLevel() {
        level.setStartTime(System.currentTimeMillis());
        level.getReadyToPlay();
        initAnimationOfPacManAndGhosts();
        if (pacBooster == PacBooster.ALWAYS_ON) {
            activatePacBooster(true);
        }
        if (level.isDemoLevel()) {
            level.showMessage(GameLevel.MESSAGE_GAME_OVER);
            score().setEnabled(true);
            highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());

        } else {
            level.showMessage(GameLevel.MESSAGE_READY);
            hud.theLevelCounter().update(level.number(), level.bonusSymbol(0));
            score().setEnabled(true);
            highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        gameEventManager.publishEvent(GameEventType.LEVEL_STARTED);
    }

    public void startNextLevel(GameContext gameContext) {
        if (level.number() < LAST_LEVEL_NUMBER) {
            buildNormalLevel(gameContext, level.number() + 1);
            startLevel();
            level.showPacAndGhosts();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", LAST_LEVEL_NUMBER);
        }
    }

    @Override
    public long pacPowerTicks(GameLevel level) {
        if (level == null) return 0;
        int index = level.number() <= 19 ? level.number() - 1 : 18;
        double seconds = POWER_PELLET_TIMES[index] / 16.0;
        return (long) (seconds * 60); // 60 ticks/sec
    }

    @Override
    public long pacPowerFadingTicks(GameLevel level) {
        return level != null ? level.data().numFlashes() * 28L : 0; // TODO check in emulator
    }

    @Override
    public void startNewGame(GameContext gameContext) {
        prepareForNewGame();
        hud.theLevelCounter().setStartLevel(startLevelNumber);
        buildNormalLevel(gameContext, startLevelNumber);
        gameEventManager.publishEvent(GameEventType.GAME_STARTED);
    }

    @Override
    public void initAnimationOfPacManAndGhosts() {
        level.pac().selectAnimation(boosterActive ? ANIM_MS_PAC_MAN_BOOSTER : ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void activatePacBooster(boolean state) {
        boosterActive = state;
        level.pac().selectAnimation(boosterActive ? ANIM_MS_PAC_MAN_BOOSTER : ANIM_PAC_MUNCHING);
    }

    @Override
    public OptionalInt cutSceneNumber(int levelNumber) {
         return switch (levelNumber) {
             case 2 -> OptionalInt.of(1);
             case 5 -> OptionalInt.of(2);
             case 9, 13, 17 -> OptionalInt.of(3);
             default -> levelNumber == LAST_LEVEL_NUMBER ? OptionalInt.of(4) : OptionalInt.empty();
        };
    }

    @Override
    public void createLevel(GameContext gameContext, int levelNumber) {
        WorldMap worldMap = mapSelector.createWorldMapForLevel(mapCategory, levelNumber);
        level = new GameLevel(levelNumber, worldMap, createLevelData());
        level.setGameOverStateTicks(420);
        addHouse(level);

        var msPacMan = createMsPacMan(gameContext);
        msPacMan.setAutopilotSteering(autopilot);
        level.setPac(msPacMan);

        level.setGhosts(
            createGhost(gameContext, RED_GHOST_SHADOW),
            createGhost(gameContext, PINK_GHOST_SPEEDY),
            createGhost(gameContext, CYAN_GHOST_BASHFUL),
            createGhost(gameContext, ORANGE_GHOST_POKEY)
        );
        level.ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setRevivalPosition(ghost.personality() == RED_GHOST_SHADOW
                ? level.ghostStartPosition(PINK_GHOST_SPEEDY)
                : level.ghostStartPosition(ghost.personality()));
        });

        // Ghosts inside house start at bottom of house instead at middle (as marked in map)
        Stream.of(PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY)
            .forEach(personality -> level.setGhostStartPosition(personality, level.ghostStartPosition(personality).plus(0, HTS))
        );

        //TODO this might not be appropriate for Tengen Ms. Pac-Man
        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));

        hud.theLevelCounter().setEnabled(levelNumber < 8);

        activatePacBooster(pacBooster == PacBooster.ALWAYS_ON);
    }

    protected void addHouse(GameLevel level) {
        WorldMap worldMap = level.worldMap();
        if (!worldMap.properties(LayerID.TERRAIN).containsKey(WorldMapProperty.POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MIN_TILE, WorldMapFormatter.formatTile(Vector2i.of(10, 15)));
        }
        if (!worldMap.properties(LayerID.TERRAIN).containsKey(WorldMapProperty.POS_HOUSE_MAX_TILE)) {
            Logger.warn("No house max tile found in map!");
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MAX_TILE, WorldMapFormatter.formatTile(Vector2i.of(17, 19)));
        }
        Vector2i houseMinTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        for (int y = 0; y < HOUSE.length; ++y) {
            for (int x = 0; x < HOUSE[y].length; ++x) {
                level.worldMap().setContent(LayerID.TERRAIN, houseMinTile.y() + y, houseMinTile.x() + x, HOUSE[y][x]);
            }
        }
        level.setGhostStartDirection(RED_GHOST_SHADOW, Direction.LEFT);
        level.setGhostStartDirection(PINK_GHOST_SPEEDY, Direction.DOWN);
        level.setGhostStartDirection(CYAN_GHOST_BASHFUL, Direction.UP);
        level.setGhostStartDirection(ORANGE_GHOST_POKEY, Direction.UP);
    }

    //TODO needed?
    private LevelData createLevelData() {
        // Note: only number of flashes is taken from level data
        return new LevelData(
            (byte) 0, // Pac speed %
            (byte) 0, // Ghost speed %
            (byte) 0, // Ghost tunnel speed %
            (byte) 0, // Elroy dots 1
            (byte) 0, // Elroy speed 1 %
            (byte) 0, // Elroy dots 2
            (byte) 0, // Elroy speed 2 %
            (byte) 0, // Pac speed powered %
            (byte) 0, // Ghost speed frightened %
            (byte) 0, // Pac power seconds
            (byte) 5  // Number of flashes
        );
    }

    @Override
    public void buildNormalLevel(GameContext gameContext, int levelNumber) {
        createLevel(gameContext, levelNumber);
        level.setDemoLevel(false);
        level.pac().immuneProperty().bind(gameContext.theGameController().propertyImmunity());
        level.pac().usingAutopilotProperty().bind(gameContext.theGameController().propertyUsingAutopilot());
        huntingTimer().reset();
        setScoreLevelNumber(levelNumber);
        gateKeeper().ifPresent(gateKeeper -> {
            gateKeeper.setLevelNumber(levelNumber);
            level.house().ifPresent(gateKeeper::setHouse); //TODO what if no house exists?
        });
        gameEventManager.publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel(GameContext gameContext) {
        createLevel(gameContext, 1);
        level.setDemoLevel(true);
        level.setGameOverStateTicks(120);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        huntingTimer.reset();
        setScoreLevelNumber(1);
        gateKeeper().ifPresent(gateKeeper -> {
            gateKeeper.setLevelNumber(1);
            level.house().ifPresent(gateKeeper::setHouse); //TODO what if no house exists?
        });
        gameEventManager.publishEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public int lastLevelNumber() { return LAST_LEVEL_NUMBER; }

    @Override
    public boolean isPacManSafeInDemoLevel() {
        float levelRunningSeconds = (System.currentTimeMillis() - level.startTime()) / 1000f;
        if (level.isDemoLevel() && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached() {
        return level.eatenFoodCount() == 64 || level.eatenFoodCount() == 176;
    }

    private byte computeBonusSymbol(int levelNumber) {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (levelNumber - 1 <= maxBonus) {
            return (byte) (levelNumber - 1);
        }
        return (byte) randomInt(0, maxBonus);
    }

    @Override
    public void activateNextBonus(GameContext gameContext) {
        //TODO Find out how Tengen really implemented this
        if (level.isBonusEdible()) {
            Logger.info("Previous bonus is still active, skip");
            return;
        }

        // compute possible bonus route
        if (level.portals().isEmpty()) {
            Logger.error("No portal found in current maze");
            return; // TODO: can this happen?
        }
        House house = level.house().orElse(null);
        if (house == null) {
            Logger.error("No house exists in game level!");
            return;
        }

        boolean leftToRight = new Random().nextBoolean();
        Vector2i houseEntry = tileAt(house.entryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        Portal entryPortal = level.portals().get(new Random().nextInt(level.portals().size()));
        Portal exitPortal  = level.portals().get(new Random().nextInt(level.portals().size()));
        List<Waypoint> route = Stream.of(
                leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
                houseEntry,
                houseEntryOpposite,
                houseEntry,
                leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(Waypoint::new).toList();

        level.selectNextBonus();
        byte symbol = level.bonusSymbol(level.currentBonusIndex());
        var bonus = new Bonus(gameContext, symbol, BONUS_VALUE_FACTORS[symbol] * 100, new Pulse(10, false));
        bonus.setEdibleTicks(TickTimer.INDEFINITE);
        bonus.setRoute(route, leftToRight);
        //bonus.setBaseSpeed(0.9f * level.speedControl().pacNormalSpeed(level)); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        gameEventManager.publishEvent(GameEventType.BONUS_ACTIVATED, bonus.tile());
    }

    @Override
    protected void checkIfPacManFindsFood(GameContext gameContext) {
        Vector2i tile = level.pac().tile();
        if (level.tileContainsFood(tile)) {
            level.pac().starvingIsOver();
            level.registerFoodEatenAt(tile);
            gateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(level));
            if (level.isEnergizerPosition(tile)) {
                simulationStep.foundEnergizerAtTile = tile;
                onEnergizerEaten();
            } else {
                scorePoints(PELLET_VALUE);
            }
            if (isBonusReached()) {
                activateNextBonus(gameContext);
                simulationStep.bonusIndex = level.currentBonusIndex();
            }
            gameEventManager.publishEvent(GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starve();
        }
    }

    private void onEnergizerEaten() {
        scorePoints(ENERGIZER_VALUE);
        Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        level.victims().clear();
        long powerTicks = pacPowerTicks(level);
        if (powerTicks > 0) {
            huntingTimer.stop();
            Logger.info("Hunting Pac-Man stopped as he got power");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / NUM_TICKS_PER_SEC);
            level.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            level.ghosts(GhostState.FRIGHTENED).forEach(Ghost::reverseAtNextOccasion);
            simulationStep.pacGotPower = true;
            gameEventManager.publishEvent(GameEventType.PAC_GETS_POWER);
        } else {
            level.ghosts(GhostState.FRIGHTENED, GhostState.HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);
        }
    }

    @Override
    public void onPacKilled() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true); // TODO how is that realized in Tengen?
        level.pac().sayGoodbyeCruelWorld();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        simulationStep.killedGhosts.add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }
}