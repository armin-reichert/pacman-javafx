/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.Waypoint;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;
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
    private static final byte BONUS_CHERRY      = 0;
    private static final byte BONUS_STRAWBERRY  = 1;
    private static final byte BONUS_ORANGE      = 2;
    private static final byte BONUS_PRETZEL     = 3;
    private static final byte BONUS_APPLE       = 4;
    private static final byte BONUS_PEAR        = 5;
            static final byte BONUS_BANANA      = 6;

    // Additional bonus symbols in Strange mazes
            static final byte BONUS_MILK        = 7;
            static final byte BONUS_ICE_CREAM   = 8;
    private static final byte BONUS_HIGH_HEELS  = 9;
    private static final byte BONUS_STAR        = 10;
    private static final byte BONUS_HAND        = 11;
    private static final byte BONUS_RING        = 12;
    private static final byte BONUS_FLOWER      = 13;

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

    private static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = {2, 4, 8, 16}; // factor * 100 = value

    public static Pac createMsPacMan() {
        var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public static Pac createPacMan() {
        var msPacMan = new Pac("Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public static Ghost createRedGhost() {
        return new Ghost(RED_GHOST_ID, "Blinky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                if (level.huntingTimer().phaseIndex() == 0) {
                    roam(speed);
                } else {
                    boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || cruiseElroy() > 0;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                    followTarget(targetTile, speed);
                }
            }

            @Override
            public Vector2i chasingTargetTile() {
                return level.pac().tile();
            }
        };
    }

    public static Ghost createPinkGhost() {
        return new Ghost(PINK_GHOST_ID, "Pinky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                if (level.huntingTimer().phaseIndex() == 0) {
                    roam(speed);
                } else {
                    boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                    Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                    followTarget(targetTile, speed);
                }
            }

            @Override
            public Vector2i chasingTargetTile() {
                return level.pac().tilesAhead(4, false);
            }
        };
    }

    public static Ghost createCyanGhost() {
        return new Ghost(CYAN_GHOST_ID, "Inky") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }

            @Override
            public Vector2i chasingTargetTile() {
                return level.pac().tilesAhead(2, false).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());
            }
        };
    }

    public static Ghost createOrangeGhost() {
        return new Ghost(ORANGE_GHOST_ID, "Sue") {
            @Override
            public void hunt() {
                float speed = level.speedControl().ghostAttackSpeed(level, this);
                boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                Vector2i targetTile = chase ? chasingTargetTile() : level.ghostScatterTile(id());
                followTarget(targetTile, speed);
            }

            @Override
            public Vector2i chasingTargetTile() {
                return tile().euclideanDist(level.pac().tile()) < 8 ? level.ghostScatterTile(id()) : level.pac().tile();
            }
        };
    }

    private final TengenMsPacMan_LevelCounter levelCounter;
    private final TengenMsPacMan_MapSelector mapSelector;
    private final TengenActorSpeedControl speedControl;
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
        speedControl = new TengenActorSpeedControl(this);
        mapSelector = new TengenMsPacMan_MapSelector();
        gateKeeper = new GateKeeper();
        huntingTimer = new TengenMsPacMan_HuntingTimer();
        huntingTimer.phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAtNextOccasion);
        });
        autopilot = new RuleBasedPacSteering(this);
        demoLevelSteering = new RuleBasedPacSteering(this);
    }

    public void init() {
        mapSelector.loadAllMaps(this);
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
        level.showMessage(GameLevel.Message.GAME_OVER);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

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
        setActorsSpeed(level);
        levelCounter().update(level.number(), level.bonusSymbol(0));
        if (level.isDemoLevel()) {
            level.showMessage(GameLevel.Message.GAME_OVER);
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());

        } else {
            level.showMessage(GameLevel.Message.READY);
            scoreManager.score().setEnabled(true);
            scoreManager.highScore().setEnabled(true);
            Logger.info("Level {} started", level.number());
        }
        // Note: This event is very important because it triggers the creation of the actor animations!
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_STARTED);
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
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.GAME_STARTED);
    }

    @Override
    public void initAnimationOfPacManAndGhosts() {
        level.pac().selectAnimation(boosterActive
            ? TengenMsPacMan_PacAnimations.ANIM_MS_PACMAN_BOOSTER : PacAnimations.ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(GhostAnimations.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void activatePacBooster(boolean active) {
        if (boosterActive != active) {
            boosterActive = active;
            float speed = speedControl.pacBaseSpeedInLevel(level.number()) + speedControl.pacDifficultySpeedDelta(difficulty);
            if (boosterActive) {
                speed += speedControl.pacBoosterSpeedDelta();
            }
            level.pac().setBaseSpeed(speed);
            level.pac().selectAnimation(boosterActive
                ? TengenMsPacMan_PacAnimations.ANIM_MS_PACMAN_BOOSTER : PacAnimations.ANIM_PAC_MUNCHING);
        }
    }

    private void setActorsSpeed(GameLevel level) {
        level.pac().setBaseSpeed(speedControl.pacBaseSpeedInLevel(level.number())
                + speedControl.pacDifficultySpeedDelta(difficulty));
        if (pacBooster == PacBooster.ALWAYS_ON) {
            activatePacBooster(true);
        }
        Logger.info("Ms. Pac-Man base speed: {0.00} px/tick", level.pac().baseSpeed());

        level.ghosts().forEach(ghost -> {
            ghost.setBaseSpeed(speedControl.ghostBaseSpeedInLevel(level.number())
                    + speedControl.ghostDifficultySpeedDelta(difficulty)
                    + speedControl.ghostIDSpeedDelta(ghost.id()));
            Logger.info("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed());
        });
    }

    @Override
    public void createLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.selectWorldMap(mapCategory, levelNumber);
        level = new GameLevel(this, levelNumber, worldMap);
        level.setData(createLevelData()); //TODO needed?
        level.setHuntingTimer(huntingTimer);
        level.setCutSceneNumber(switch (levelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> levelNumber == LAST_LEVEL_NUMBER ? 4 : 0;
        });
        level.setGameOverStateTicks(420);
        level.addArcadeHouse();

        var msPacMan = createMsPacMan();
        msPacMan.setGameLevel(level);
        msPacMan.setAutopilot(autopilot);
        level.setPac(msPacMan);

        //TODO clarify hunting behavior
        level.setGhosts(
            createRedGhost(),
            createPinkGhost(),
            createCyanGhost(),
            createOrangeGhost()
        );
        level.ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setRevivalPosition(ghost.id() == RED_GHOST_ID
                ? level.ghostStartPosition(PINK_GHOST_ID)
                : level.ghostStartPosition(ghost.id()));
            ghost.setGameLevel(level);
        });

        // Ghosts inside house start at bottom of house instead at middle (as marked in map)
        Stream.of(PINK_GHOST_ID, CYAN_GHOST_ID, ORANGE_GHOST_ID)
            .forEach(id -> level.setGhostStartPosition(id,
                level.ghostStartPosition(id).plus(0, HTS))
        );

        level.setSpeedControl(speedControl);

        // Must be called after creation of the actors!
        setActorsSpeed(level);

        //TODO this might not be appropriate for Tengen Ms. Pac-Man
        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));

        levelCounter.setEnabled(levelNumber < 8);
        activatePacBooster(false); // gets activated in startLevel() if mode is ALWAYS_ON
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
        scoreManager.setScoreLevelNumber(levelNumber);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(levelNumber));
        level.huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }


    @Override
    public void buildDemoLevel() {
        createLevel(1);
        level.setDemoLevel(true);
        level.setGameOverStateTicks(120);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        scoreManager.setScoreLevelNumber(1);
        gateKeeper().ifPresent(gateKeeper -> gateKeeper.setLevelNumber(1));
        level.huntingTimer().reset();
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.LEVEL_CREATED);
    }

    @Override
    public int lastLevelNumber() {
        return LAST_LEVEL_NUMBER;
    }

    @Override
    public void assignDemoLevelBehavior(Pac pac) {
        pac.setAutopilot(demoLevelSteering);
        pac.setUsingAutopilot(true);
        pac.setImmune(false);
    }

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
        List<Portal> portals = level.portals().toList();
        if (portals.isEmpty()) {
            Logger.error("No portal found in current maze");
            return; // TODO: can this happen?
        }
        boolean leftToRight = THE_RNG.nextBoolean();
        Vector2i houseEntry = tileAt(level.houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, level.houseSizeInTiles().y() + 1);
        Portal entryPortal = portals.get(THE_RNG.nextInt(portals.size()));
        Portal exitPortal  = portals.get(THE_RNG.nextInt(portals.size()));
        List<Waypoint> route = Stream.of(
                leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
                houseEntry,
                houseEntryOpposite,
                houseEntry,
                leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(Waypoint::new).toList();

        level.selectNextBonus();
        byte symbol = level.bonusSymbol(level.currentBonusIndex());
        var bonus = new MovingBonus(level, symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        bonus.setEdibleTicks(TickTimer.INDEFINITE);
        bonus.setRoute(route, leftToRight);
        bonus.setBaseSpeed(0.9f * level.speedControl().pacNormalSpeed(level)); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");

        level.setBonus(bonus);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.BONUS_ACTIVATED, bonus.actor().tile());
    }

    @Override
    protected void checkIfPacManFindsFood() {
        Vector2i tile = level.pac().tile();
        if (level.hasFoodAt(tile)) {
            level.pac().starvingEnds();
            level.registerFoodEatenAt(tile);
            gateKeeper().ifPresent(gateKeeper -> gateKeeper.registerFoodEaten(level));
            if (level.isEnergizerPosition(tile)) {
                THE_SIMULATION_STEP.setFoundEnergizerAtTile(tile);
                onEnergizerEaten(tile);
            } else {
                scoreManager.scorePoints(PELLET_VALUE);
            }
            if (isBonusReached()) {
                activateNextBonus();
                THE_SIMULATION_STEP.setBonusIndex(level.currentBonusIndex());
            }
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starvingContinues();
        }
    }

    private void onEnergizerEaten(Vector2i tile) {
        scoreManager.scorePoints(ENERGIZER_VALUE);
        Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        level.victims().clear();
        long powerTicks = pacPowerTicks(level);
        if (powerTicks > 0) {
            level.huntingTimer().stop();
            Logger.info("Hunting Pac-Man stopped as he got power");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / NUM_TICKS_PER_SEC);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            level.ghosts(FRIGHTENED).forEach(Ghost::reverseAtNextOccasion);
            THE_SIMULATION_STEP.setPacGotPower();
            THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.PAC_GETS_POWER);
        } else {
            level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseAtNextOccasion);
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
        level.pac().die();
    }

    @Override
    public void onGhostKilled(Ghost ghost) {
        THE_SIMULATION_STEP.killedGhosts().add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }
}