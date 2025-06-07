/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.Waypoint;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.steering.Steering;
import de.amr.pacmanfx.ui.PacManGames_Env;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_PacAnimationMap.ANIM_MS_PAC_MAN_BOOSTER;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @author Armin Reichert
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

    public static Pac createMsPacMan() {
        var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public static Pac createPacMan() {
        var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public static Ghost createRedGhost() {
        return new Ghost(RED_GHOST_SHADOW, "Blinky") {
            @Override
            public void hunt(GameLevel level) {
                float speed = theGame().actorSpeedControl().ghostAttackSpeed(level, this);
                setSpeed(speed);
                if (theGame().huntingTimer().phaseIndex() == 0) {
                    roam(level);
                } else {
                    boolean chase = theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile(level) : level.ghostScatterTile(personality());
                    tryMovingTowardsTargetTile(level, targetTile);
                }
            }

            @Override
            public Vector2i chasingTargetTile(GameLevel level) {
                return level.pac().tile();
            }
        };
    }

    public static Ghost createPinkGhost() {
        return new Ghost(PINK_GHOST_SPEEDY, "Pinky") {
            @Override
            public void hunt(GameLevel level) {
                float speed = theGame().actorSpeedControl().ghostAttackSpeed(level, this);
                setSpeed(speed);
                if (theGame().huntingTimer().phaseIndex() == 0) {
                    roam(level);
                } else {
                    boolean chase = theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile(level) : level.ghostScatterTile(personality());
                    tryMovingTowardsTargetTile(level, targetTile);
                }
            }

            @Override
            public Vector2i chasingTargetTile(GameLevel level) {
                return level.pac().tilesAhead(4);
            }
        };
    }

    public static Ghost createCyanGhost() {
        return new Ghost(CYAN_GHOST_BASHFUL, "Inky") {
            @Override
            public void hunt(GameLevel level) {
                float speed = theGame().actorSpeedControl().ghostAttackSpeed(level, this);
                boolean chase = theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile(level) : level.ghostScatterTile(personality());
                setSpeed(speed);
                tryMovingTowardsTargetTile(level, targetTile);
            }

            @Override
            public Vector2i chasingTargetTile(GameLevel level) {
                return level.pac().tilesAhead(2).scaled(2).minus(level.ghost(RED_GHOST_SHADOW).tile());
            }
        };
    }

    public static Ghost createOrangeGhost() {
        return new Ghost(ORANGE_GHOST_POKEY, "Sue") {
            @Override
            public void hunt(GameLevel level) {
                float speed = theGame().actorSpeedControl().ghostAttackSpeed(level, this);
                boolean chase = theGame().huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile(level) : level.ghostScatterTile(personality());
                setSpeed(speed);
                tryMovingTowardsTargetTile(level, targetTile);
            }

            @Override
            public Vector2i chasingTargetTile(GameLevel level) {
                return tile().euclideanDist(level.pac().tile()) < 8 ? level.ghostScatterTile(personality()) : level.pac().tile();
            }
        };
    }

    private final TengenMsPacMan_LevelCounter levelCounter;
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

    public TengenMsPacMan_GameModel() {
        scoreManager.setHighScoreFile(new File(HOME_DIR, "highscore-ms_pacman_tengen.xml"));
        levelCounter = new TengenMsPacMan_LevelCounter();
        actorSpeedControl = new TengenActorSpeedControl();
        mapSelector = new TengenMsPacMan_MapSelector();
        gateKeeper = new GateKeeper(); //TODO implement Tengen logic
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
        levelCounter.clear();
        playingProperty().set(false);
        boosterActive = false;
        scoreManager.loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
    }

    @Override
    public void onGameEnding() {
        playingProperty().set(false);
        scoreManager.updateHighScore();
        level.showMessage(GameLevel.MESSAGE_GAME_OVER);
        theGameEventManager().publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public ActorSpeedControl actorSpeedControl() { return actorSpeedControl; }

    @Override
    public HuntingTimer huntingTimer() { return huntingTimer; }

    @Override
    public Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public LevelCounter levelCounter() { return levelCounter; }

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
            scoreManager.setExtraLifeScores(10_000, 970_000, 980_000, 990_000);
        } else {
            scoreManager.setExtraLifeScores(10_000, 50_000, 100_000, 300_000);
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
        level.makeReadyForPlaying();
        initAnimationOfPacManAndGhosts();
        if (pacBooster == PacBooster.ALWAYS_ON) {
            activatePacBooster(true);
        }
        if (level.isDemoLevel()) {
            level.showMessage(GameLevel.MESSAGE_GAME_OVER);
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());

        } else {
            level.showMessage(GameLevel.MESSAGE_READY);
            levelCounter().update(level.number(), level.bonusSymbol(0));
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_STARTED);
    }

    public void startNextLevel() {
        if (level.number() < LAST_LEVEL_NUMBER) {
            buildNormalLevel(level.number() + 1);
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
    public boolean isOver() { return lifeCount() == 0; }

    @Override
    public void startNewGame() {
        prepareForNewGame();
        levelCounter.setStartLevel(startLevelNumber);
        buildNormalLevel(startLevelNumber);
        theGameEventManager().publishEvent(this, GameEventType.GAME_STARTED);
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
    public void createLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.createWorldMapForLevel(mapCategory, levelNumber);
        level = new GameLevel(levelNumber, worldMap, createLevelData());
        level.setGameOverStateTicks(420);
        addHouse(level);

        var msPacMan = createMsPacMan();
        msPacMan.setAutopilotSteering(autopilot);
        level.setPac(msPacMan);

        level.setGhosts(createRedGhost(), createPinkGhost(), createCyanGhost(), createOrangeGhost());
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

        levelCounter.setEnabled(levelNumber < 8);

        activatePacBooster(pacBooster == PacBooster.ALWAYS_ON);
    }

    private static final byte[][] HOUSE = {
        { ARC_NW.code(), WALL_H.code(), WALL_H.code(), DOOR.code(), DOOR.code(), WALL_H.code(), WALL_H.code(), ARC_NE.code() },
        { WALL_V.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), WALL_V.code()   },
        { WALL_V.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), EMPTY.code(), WALL_V.code()   },
        { ARC_SW.code(), WALL_H.code(), WALL_H.code(), WALL_H.code(), WALL_H.code(), WALL_H.code(), WALL_H.code(), ARC_SE.code() }
    };

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
        level.setLeftDoorTile(houseMinTile.plus(3, 0));
        level.setRightDoorTile(houseMinTile.plus(4, 0));
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
    public void buildNormalLevel(int levelNumber) {
        createLevel(levelNumber);
        level.setDemoLevel(false);
        level.pac().immuneProperty().bind(PacManGames_Env.PY_IMMUNITY);
        level.pac().usingAutopilotProperty().bind(PacManGames_Env.PY_USING_AUTOPILOT);
        huntingTimer().reset();
        scoreManager.setScoreLevelNumber(levelNumber);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(levelNumber));
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public void buildDemoLevel() {
        createLevel(1);
        level.setDemoLevel(true);
        level.setGameOverStateTicks(120);
        level.pac().setImmune(false);
        level.pac().setUsingAutopilot(true);
        level.pac().setAutopilotSteering(demoLevelSteering);
        demoLevelSteering.init();
        huntingTimer.reset();
        scoreManager.setScoreLevelNumber(1);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(1));
        theGameEventManager().publishEvent(this, GameEventType.LEVEL_CREATED);
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
    public void activateNextBonus() {
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
        boolean leftToRight = theRNG().nextBoolean();
        Vector2i houseEntry = tileAt(level.houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, level.houseSizeInTiles().y() + 1);
        Portal entryPortal = level.portals().get(theRNG().nextInt(level.portals().size()));
        Portal exitPortal  = level.portals().get(theRNG().nextInt(level.portals().size()));
        List<Waypoint> route = Stream.of(
                leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
                houseEntry,
                houseEntryOpposite,
                houseEntry,
                leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(Waypoint::new).toList();

        level.selectNextBonus();
        byte symbol = level.bonusSymbol(level.currentBonusIndex());
        var bonus = new MovingBonus(symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        bonus.setEdibleTicks(TickTimer.INDEFINITE);
        bonus.setRoute(route, leftToRight);
        //bonus.setBaseSpeed(0.9f * level.speedControl().pacNormalSpeed(level)); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        theGameEventManager().publishEvent(this, GameEventType.BONUS_ACTIVATED, bonus.actor().tile());
    }

    @Override
    protected void checkIfPacManFindsFood() {
        Vector2i tile = level.pac().tile();
        if (level.tileContainsFood(tile)) {
            level.pac().starvingIsOver();
            level.registerFoodEatenAt(tile);
            gateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(level));
            if (level.isEnergizerPosition(tile)) {
                theSimulationStep().foundEnergizerAtTile = tile;
                onEnergizerEaten();
            } else {
                scoreManager.scorePoints(PELLET_VALUE);
            }
            if (isBonusReached()) {
                activateNextBonus();
                theSimulationStep().bonusIndex = level.currentBonusIndex();
            }
            theGameEventManager().publishEvent(this, GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starve();
        }
    }

    private void onEnergizerEaten() {
        scoreManager.scorePoints(ENERGIZER_VALUE);
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
            theSimulationStep().pacGotPower = true;
            theGameEventManager().publishEvent(this, GameEventType.PAC_GETS_POWER);
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
        theSimulationStep().killedGhosts.add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_FACTORS[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }
}