/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
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

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.SpeedConfiguration.*;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @author Armin Reichert
 */
public class TengenMsPacManGame extends GameModel {

    public static final int MIN_LEVEL_NUMBER = 1;
    public static final int MAX_LEVEL_NUMBER = 32;

    // Animation IDs specific to this game
    public static final String ANIM_MS_PACMAN_BOOSTER = "ms_pacman_booster";
    public static final String ANIM_PACMAN_BOOSTER    = "pacman_booster";

    static final byte PELLET_VALUE = 10;
    static final byte ENERGIZER_VALUE = 50;

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
    static final byte[] BONUS_VALUE_FACTORS = new byte[14];
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

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    private static final LevelData DUMMY_LEVEL_DATA = new LevelData(new byte[] {
        100, // Pac speed in % of base speed
        100, // Ghost speed in % of base speed
        40, // Ghost speed in tunnel...
        20, // Dots left for Elroy1
        105, // Elroy1 speed...
        10, // Dots left for Elroy2
        110, // Elroy2 speed
        110, // Pac (power mode) speed...
        50, // Frightened ghost speed...
        6, // pac power time (seconds)
        5, // Num flashes
    });

    private final MapConfigurationManager mapConfigMgr = new MapConfigurationManager();
    private MapCategory mapCategory;
    private Difficulty difficulty;
    private BoosterMode boosterMode;
    private boolean boosterActive;
    private byte startLevelNumber; // 1-7
    private boolean canStartGame;
    private byte numContinues;
    private final Steering autopilot = new RuleBasedPacSteering(this);
    private final Steering demoLevelSteering = new RuleBasedPacSteering(this);

    public TengenMsPacManGame(File userDir) {
        super(userDir);
        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman_tengen.xml"));
        simulateOverflowBug = false;

        //TODO: I have no info about the exact timing so far, so I use these (inofficial) Arcade game values for now
        huntingControl = new HuntingControl() {
            static final int[] TICKS_LEVEL_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
            static final int[] TICKS_LEVEL_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? TICKS_LEVEL_1_TO_4[phaseIndex] : TICKS_LEVEL_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingControl.setOnPhaseChange(() -> level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseASAP));

        mapConfigMgr.loadMaps();
        reset();
    }

    @Override
    public void reset() {
        level = null;
        demoLevel = false;
        playing = false;
        lives = initialLives;
        boosterActive = false;
        setMapCategory(MapCategory.ARCADE);
        setBoosterMode(BoosterMode.OFF);
        setDifficulty(Difficulty.NORMAL);
        setStartLevelNumber(1);
        setInitialLives(3);
        setNumContinues(4);
        levelCounter().clear();
        scoreManager().loadHighScore();
        scoreManager.resetScore();
    }

    @Override
    public void endGame() {
        scoreManager().updateHighScore();
        publishGameEvent(GameEventType.STOP_ALL_SOUNDS);
    }

    public MapConfigurationManager mapConfigMgr() {
        return mapConfigMgr;
    }

    public void setBoosterMode(BoosterMode mode) {
        boosterMode = mode;
    }

    public BoosterMode boosterMode() {
        return boosterMode;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = checkNotNull(mapCategory);
        if (mapCategory == MapCategory.ARCADE) {
            scoreManager.setExtraLifeScores(10_000);
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
        this.startLevelNumber = (byte) number;
    }

    public byte startLevelNumber() {
        return startLevelNumber;
    }

    public void setNumContinues(int numContinues) {
        this.numContinues = (byte) numContinues;
    }

    public byte numContinues() {
        return numContinues;
    }

    public boolean isBoosterActive() {
        return boosterActive;
    }

    //TODO remove this only for info panel in dashboard
    public Optional<LevelData> currentLevelData() {
        return Optional.of(DUMMY_LEVEL_DATA);
    }

    @Override
    public boolean canStartNewGame() {
        return canStartGame;
    }

    public void setCanStartGame(boolean canStartGame) {
        this.canStartGame = canStartGame;
    }

    @Override
    public long gameOverStateTicks() {
        return 420; // TODO how much really?
    }

    @Override
    public void startLevel() {
        super.startLevel();
        scoreManager.setScoreEnabled(true);
    }

    @Override
    public long pacPowerTicks() {
        if (!inRange(level.number, MIN_LEVEL_NUMBER, MAX_LEVEL_NUMBER)) {
            return 0;
        }
        double seconds = switch (level.number) {
            case  1 -> 6;
            case  2 -> 5;
            case  3 -> 4;
            case  4 -> 3;
            case  5 -> 2;
            case  6 -> 5;
            case  7 -> 2;
            case  8 -> 1.75;
            case  9 -> 1.5;
            case 10 -> 4;
            case 11 -> 2;
            case 12 -> 1.75;
            case 13 -> 1.5;
            case 14 -> 2;
            case 15 -> 1.75;
            case 16 -> 1.5;
            case 17 -> 0;
            case 18 -> 1.5;
            default -> 2;
        };
        return (long) (seconds * 60);
    }

    @Override
    public long pacPowerFadingTicks() {
        return numFlashes() * 28L; // TODO check in emulator
    }

    @Override
    public float pacNormalSpeed() {
        return level.pac() != null ? level.pac().baseSpeed() : 0;
    }

    @Override
    public float pacPowerSpeed() {
        if (level != null) {
            //TODO is this correct?
            return 1.1f * level.pac().baseSpeed();
        }
        return 0;
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        if (level == null) {
            return 0;
        }
        if (level.world().isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(ghost);
        }
        float speed = ghost.baseSpeed();
        float increase = ghostSpeedIncreaseByFoodRemaining(this);
        if (increase > 0) {
            speed += increase;
            Logger.info("Ghost speed increased by {0} units to {0.00} px/tick for {}", increase, speed, ghost.name());
        }
        return speed;
    }

    @Override
    public float ghostSpeedInsideHouse(Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostFrightenedSpeed(Ghost ghost) {
        //TODO is this correct?
        return 0.5f * ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        //TODO is this correct?
        return 0.4f * ghost.baseSpeed();
    }

    @Override
    public void loseLife() {
        if (lives > 0) {
            --lives;
        } else if (numContinues > 0) {
            --numContinues;
        }
    }

    @Override
    public boolean isOver() {
        return lives == 0;
    }

    @Override
    public void startNewGame() {
        reset();
        createLevel(startLevelNumber);
        if (startLevelNumber > 1) {
            levelCounter.clear();
            for (int number = 1; number <= Math.min(startLevelNumber, LEVEL_COUNTER_MAX_SIZE); ++number) {
                levelCounter.add((byte) (number - 1));
            }
        }
        publishGameEvent(GameEventType.GAME_STARTED);
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        float pacBaseSpeed = pacBaseSpeedInLevel(levelNumber) + pacDifficultySpeedDelta(difficulty);
        level.pac().setBaseSpeed(pacBaseSpeed);
        if (boosterMode == BoosterMode.ALWAYS_ON) {
            activatePacBooster(true);
        }
        level.ghosts().forEach(ghost -> {
            ghost.setBaseSpeed(ghostBaseSpeedInLevel(levelNumber)
                + ghostDifficultySpeedDelta(difficulty) + ghostIDSpeedDelta(ghost.id()));
        });
    }

    @Override
    protected void initActorAnimations() {
        level.pac().selectAnimation(boosterActive ? ANIM_MS_PACMAN_BOOSTER : ANIM_PAC_MUNCHING);
        level.pac().animations().ifPresent(Animations::resetCurrentAnimation);
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void activatePacBooster(boolean active) {
        if (boosterActive != active) {
            boosterActive = active;
            float speed = pacBaseSpeedInLevel(level.number) + pacDifficultySpeedDelta(difficulty);
            if (boosterActive) {
                speed += pacBoosterSpeedDelta();
            }
            level.pac().setBaseSpeed(speed);
            level.pac().selectAnimation(boosterActive ? ANIM_MS_PACMAN_BOOSTER : ANIM_PAC_MUNCHING);
        }
    }

    protected void createWorldAndPopulation(WorldMap map) {
        level.setWorld(new GameWorld(map));
        level.world().createArcadeHouse(10, 15);
        Logger.info("World created. Map config: {}, URL: {}", level.mapConfig(), level.mapConfig().worldMap().url());

        var pac = new Pac();
        pac.setName("Ms. Pac-Man");
        pac.setWorld(level.world());
        pac.reset();
        level.setPac(pac);

        var ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setWorld(level.world());
            ghost.reset();
            ghost.setRevivalPosition(level.world().ghostPosition(ghost.id()));
        });
        ghosts[RED_GHOST].setRevivalPosition(level.world().ghostPosition(PINK_GHOST)); // middle house position
        level.setGhosts(ghosts);

        //TODO this might not be appropriate for Tengen Ms. Pac-Man
        level.setBonusSymbol(0, computeBonusSymbol());
        level.setBonusSymbol(1, computeBonusSymbol());
    }

    @Override
    public void configureNormalLevel() {
        levelCounterEnabled = level.number < 8;
        level.setMapConfig(mapConfigMgr.getMapConfig(mapCategory, level.number));
        createWorldAndPopulation(level.mapConfig().worldMap());
        level.pac().setAutopilot(autopilot);
        activatePacBooster(false); // gets activated in startLevel() if mode is ALWAYS_ON
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
        // ghosts inside house start at floor of house
        level.ghosts().filter(ghost -> ghost.id() != GameModel.RED_GHOST).forEach(ghost -> {
            level.world().setGhostPosition(ghost.id(), level.world().ghostPosition(ghost.id()).plus(0, HTS));
        });
    }

    @Override
    public void configureDemoLevel() {
        levelCounterEnabled = false;
        demoLevelSteering.init();
        level.setMapConfig(mapConfigMgr.getMapConfig(mapCategory, level.number));
        createWorldAndPopulation(level.mapConfig().worldMap());
        activatePacBooster(false); // gets activated in startLevel() if mode is ALWAYS_ON
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
        // ghosts inside house start at floor of house
        level.ghosts().filter(ghost -> ghost.id() != GameModel.RED_GHOST).forEach(ghost -> {
            level.world().setGhostPosition(ghost.id(), level.world().ghostPosition(ghost.id()).plus(0, HTS));
        });
        setDemoLevelBehavior();
    }

    @Override
    public void setDemoLevelBehavior() {
        level.pac().setAutopilot(demoLevelSteering);
        level.pac().setUsingAutopilot(true);
        level.pac().setImmune(false);
    }

    @Override
    public int numFlashes() {
        //TODO need to find out what Tengen really does
        return 5;
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return switch (level.number) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13 -> 3; // TODO not sure what happens in later levels
            case MAX_LEVEL_NUMBER -> 4;
            default -> 0;
        };
    }

    @Override
    public boolean isPacManKillingIgnored() {
        float levelRunningSeconds = (System.currentTimeMillis() - level.startTime()) / 1000f;
        if (isDemoLevel() && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached() {
        return level.world().eatenFoodCount() == 64 || level.world().eatenFoodCount() == 176;
    }

    @Override
    public byte computeBonusSymbol() {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (level.number - 1 <= maxBonus) {
            return (byte) (level.number - 1);
        }
        return (byte) randomInt(0, maxBonus);
    }

    @Override
    public void activateNextBonus() {
        //TODO No idea how this behaves in Tengen
        if (level.bonus().isPresent() && level.bonus().get().state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        level.advanceNextBonus();

        boolean leftToRight = RND.nextBoolean();
        Vector2i houseEntry = tileAt(level.world().houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, level.world().houseSize().y() + 1);
        List<Portal> portals = level.world().portals().toList();
        if (portals.isEmpty()) {
            return; // there should be no mazes without portal but who knows?
        }
        Portal entryPortal = portals.get(RND.nextInt(portals.size()));
        Portal exitPortal  = portals.get(RND.nextInt(portals.size()));
        List<NavPoint> route = Stream.of(
            leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(NavPoint::np).toList();

        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        var movingBonus = new MovingBonus(level.world(), symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setRoute(route, leftToRight);
        movingBonus.setBaseSpeed(1f); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        level.setBonus(movingBonus);
        movingBonus.setEdible(TickTimer.INDEFINITE);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, movingBonus.entity().tile());
    }

    @Override
    protected void onPelletOrEnergizerEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        //TODO does Ms. Pac-Man slow down after eating here too?
        //pac.setRestingTicks(energizer ? 3 : 1);
        if (energizer) {
            processEatenEnergizer();
            scoreManager.scorePoints(this, ENERGIZER_VALUE);
            Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        } else {
            scoreManager.scorePoints(this, PELLET_VALUE);
        }
        gateKeeper.registerFoodEaten(level);
        if (isBonusReached()) {
            activateNextBonus();
            eventLog.bonusIndex = level.nextBonusIndex();
        }
    }

    @Override
    public void onPacKilled() {
        huntingControl.stop();
        Logger.info("Hunting timer stopped");
        level.powerTimer().stop();
        level.powerTimer().reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true); // TODO how is that realized in Tengen?
        level.pac().die();
    }

    @Override
    protected void onGhostReleased(Ghost ghost) {
        // code that is executed when ghost is released from jailhouse
    }

    // TODO clarify what exactly Tengen Ms. Pac-Man does
    private void ghostHuntingBehaviour(Ghost ghost) {
        float speed = ghostAttackSpeed(ghost);
        if (huntingControl.phaseIndex() == 0 && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghost.roam(speed);
        } else {
            boolean chasing = huntingControl.phaseType() == HuntingControl.PhaseType.CHASING;
            Vector2i targetTile = chasing ? chasingTarget(ghost) : scatterTarget(ghost);
            ghost.followTarget(targetTile, speed);
        }
    }
}