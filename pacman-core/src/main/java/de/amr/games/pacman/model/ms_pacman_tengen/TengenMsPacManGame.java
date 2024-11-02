/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.ANIM_GHOST_NORMAL;
import static de.amr.games.pacman.model.GameModel.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.SpeedConfiguration.*;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @author Armin Reichert
 */
public class TengenMsPacManGame extends GameModel {

    public static int MIN_LEVEL_NUMBER = 1;
    public static int MAX_LEVEL_NUMBER = 32;

    // Animation IDs specific to this game
    public static final String ANIM_MS_PACMAN_BOOSTER = "ms_pacman_booster";
    public static final String ANIM_PACMAN_BOOSTER    = "pacman_booster";

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

    private final MapConfigurationManager mapConfigMgr = new MapConfigurationManager();

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private BoosterMode boosterMode;
    private boolean boosterActive;
    private byte startLevelNumber; // 1-7
    private boolean canStartGame;

    private LevelData currentLevelData; // TODO

    public TengenMsPacManGame(GameVariant gameVariant, File userDir) {
        super(gameVariant, userDir);

        initialLives = 3;
        scoreManager.setHighScoreFile(new File(userDir, "highscore-ms_pacman_tengen.xml"));

        //TODO: I have no idea about the timing in Tengen, use these inofficial Ms. Pac-Man Arcade values for now
        huntingControl = new HuntingControl("HuntingControl-" + getClass().getSimpleName()) {
            private static final int[] HUNTING_TICKS_1_TO_4 = {420, 1200, 1, 62220, 1, 62220, 1, -1};
            private static final int[] HUNTING_TICKS_5_PLUS = {300, 1200, 1, 62220, 1, 62220, 1, -1};

            @Override
            public long huntingTicks(int levelNumber, int phaseIndex) {
                long ticks = levelNumber < 5 ? HUNTING_TICKS_1_TO_4[phaseIndex] : HUNTING_TICKS_5_PLUS[phaseIndex];
                return ticks != -1 ? ticks : TickTimer.INDEFINITE;
            }
        };
        huntingControl.setOnPhaseChange(() -> ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible));

        setMapCategory(MapCategory.ARCADE);
        setBoosterMode(BoosterMode.OFF);
        setDifficulty(Difficulty.NORMAL);
        setStartLevelNumber(1);
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

    public boolean isBoosterActive() {
        return boosterActive;
    }

    // only for info panel in dashboard
    public Optional<LevelData> currentLevelData() {
        return Optional.ofNullable(currentLevelData);
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
        return 600; // TODO how much really?
    }

    @Override
    protected void initScore(int levelNumber) {
        scoreManager.setScoreEnabled(levelNumber > 0);
        scoreManager.setHighScoreEnabled(levelNumber > 0 && !isDemoLevel());
    }

    @Override
    protected Pac createPac() {
        Pac msPacMan = new Pac();
        msPacMan.setName("Ms. Pac-Man");
        return msPacMan;
    }

    @Override
    public long pacPowerTicks() {
        if (!inRange(currentLevelNumber, MIN_LEVEL_NUMBER, MAX_LEVEL_NUMBER)) {
            return 0;
        }
        double seconds = switch (currentLevelNumber) {
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
        return pac != null ? pac.baseSpeed() : 0;
    }

    @Override
    public float pacPowerSpeed() {
        if (pac != null) {
            float percentage = currentLevelData.pacSpeedPoweredPercentage();
            return percentage > 0 ? percentage * 0.01f * pac.baseSpeed() : pac.baseSpeed();
        }
        return 0;
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        if (world == null) {
            return 0;
        }
        if (world.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(ghost);
        }
        float speed = ghost.baseSpeed();
        float increase = SpeedConfiguration.ghostSpeedIncreaseByFoodRemaining(this);
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
        float percentage = currentLevelData.ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * 0.01f * ghost.baseSpeed() : ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return currentLevelData.ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
    }

    @Override
    protected boolean hasOverflowBug() {
        return false;
    }

    @Override
    protected Ghost[] createGhosts() {
        return new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
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
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        float pacBaseSpeed = pacBaseSpeedInLevel(levelNumber) + pacDifficultySpeedDelta(difficulty);
        pac.setBaseSpeed(pacBaseSpeed);
        if (boosterMode == BoosterMode.ALWAYS_ON) {
            activateBooster();
        }
        for (Ghost ghost : ghosts) {
            ghost.setBaseSpeed(ghostBaseSpeedInLevel(levelNumber)
                + ghostDifficultySpeedDelta(difficulty) + ghostIDSpeedDelta(ghost.id()));
        }
    }

    @Override
    protected void initActorAnimations() {
        pac.selectAnimation(boosterActive ? ANIM_MS_PACMAN_BOOSTER : ANIM_PAC_MUNCHING);
        pac.animations().ifPresent(Animations::resetCurrentAnimation);
        ghosts().forEach(ghost -> {
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void activateBooster() {
        if (boosterActive) {
            Logger.warn("Pac booster is already active");
            return;
        }
        boosterActive = true;
        pac.setBaseSpeed(pacBaseSpeedInLevel(currentLevelNumber) + pacDifficultySpeedDelta(difficulty) + pacBoosterSpeedDelta());
        pac.selectAnimation(ANIM_MS_PACMAN_BOOSTER);
        Logger.info("Ms. Pac-Man booster activated, base speed set to {0.00} px/s", pac.baseSpeed());
    }

    public void deactivateBooster() {
        if (!boosterActive) {
            Logger.warn("Pac booster is already inactive");
            return;
        }
        boosterActive = false;
        pac.setBaseSpeed(pacBaseSpeedInLevel(currentLevelNumber) + pacDifficultySpeedDelta(difficulty));
        pac.selectAnimation(ANIM_PAC_MUNCHING);
        Logger.info("Ms. Pac-Man booster deactivated, base speed set to {0.00} px/s", pac.baseSpeed());
    }

    @Override
    public void buildLevel(int levelNumber) {
        if (!inRange(levelNumber, MIN_LEVEL_NUMBER, MAX_LEVEL_NUMBER)) {
            throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        }
        currentLevelNumber = levelNumber;

        MapConfig mapConfig = mapConfigMgr.getMapConfig(mapCategory, currentLevelNumber);
        currentMapNumber = mapConfig.mapNumber();
        currentMap = mapConfig.worldMap();
        currentMapColorScheme = mapConfig.colorScheme();

        createWorldAndPopulation(currentMap);
        Logger.info("World created. Map number: {}, URL: {}", currentMapNumber, currentMap.url());

        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUsingAutopilot(false);
        deactivateBooster(); // gets activated in startLevel() if ALWAYS_ON

        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));

        // TODO: change this. For now provide a level object such that all code that relies on existing level object still works
        currentLevelData = new LevelData(new byte[] {
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
            0 // cut scene after this level
        });
    }

    @Override
    public void buildDemoLevel() {
        currentLevelNumber = 1;

        MapConfig mapConfig = mapConfigMgr.getMapConfig(mapCategory, currentLevelNumber);
        currentMapNumber = mapConfig.mapNumber();
        currentMap = mapConfig.worldMap();
        currentMapColorScheme = mapConfig.colorScheme();

        createWorldAndPopulation(currentMap);

        pac.setAutopilot(new RuleBasedPacSteering(this));
        pac.setUsingAutopilot(true);
        deactivateBooster(); // gets activated in startLevel() if ALWAYS_ON

        ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));

        // TODO for now provide a Level object such that all code that relies on one works
        currentLevelData = new LevelData(new byte[]{
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
            0 // cut scene after this level
        });
    }

    @Override
    public void letsGetReadyToRumble() {
        super.letsGetReadyToRumble();
        // ghosts inside house start at floor of house
        float posY = world.houseFloorY() - HTS;
        ghosts().filter(Ghost::insideHouse).forEach(ghost -> ghost.setPosY(posY));
    }

    @Override
    public int numFlashes() {
        //TODO need to find out what Tengen really does
        return 5;
    }

    @Override
    public int intermissionNumberAfterLevel() {
        return switch (currentLevelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3; // TODO not sure what happens in later levels
            default -> 0;
        };
    }

    /** In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
     * (also inside a level) whenever a bonus score is reached. At least that's what I was told.
     */
    @Override
    protected boolean isLevelCounterEnabled() {
        return currentLevelNumber < 8 && !demoLevel;
    }

    @Override
    public boolean isPacManKillingIgnoredInDemoLevel() {
        float levelRunningSeconds = (System.currentTimeMillis() - levelStartTime) / 1000f;
        if (demoLevel && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
            Logger.info("Pac-Man dead ignored, demo level is running since {} seconds", levelRunningSeconds);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBonusReached() {
        return world.eatenFoodCount() == 64 || world.eatenFoodCount() == 176;
    }

    @Override
    public byte computeBonusSymbol() {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (currentLevelNumber - 1 <= maxBonus) {
            return (byte) (currentLevelNumber - 1);
        }
        return (byte) randomInt(0, maxBonus);
    }

    @Override
    public void activateNextBonus() {
        //TODO No idea how this behaves in Tengen
        if (bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
            Logger.info("Previous bonus is still active, skip this one");
            return;
        }
        nextBonusIndex += 1;

        boolean leftToRight = RND.nextBoolean();
        Vector2i houseEntry = tileAt(world.houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, world.houseSize().y() + 1);
        List<Portal> portals = world.portals().toList();
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

        byte symbol = bonusSymbols[nextBonusIndex];
        var movingBonus = new MovingBonus(world, symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setRoute(route, leftToRight);
        movingBonus.setBaseSpeed(1f); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        bonus = movingBonus;
        bonus.setEdible(TickTimer.INDEFINITE);
        publishGameEvent(GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
    }

    @Override
    protected GameWorld createWorld(WorldMap map) {
        var world = new GameWorld(map);
        Vector2i houseTopLeftTile = map.terrain().getTileProperty(GameWorld.PROPERTY_POS_HOUSE_MIN_TILE, v2i(10, 15));
        world.createArcadeHouse(houseTopLeftTile.x(), houseTopLeftTile.y());
        return world;
    }

    @Override
    public void onGameEnded() {
        // nothing to do yet
    }

    @Override
    protected void onPelletOrEnergizerEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        //TODO does Ms. Pac-Man slow down after eating here too?
        //pac.setRestingTicks(energizer ? 3 : 1);
        if (energizer) {
            processEatenEnergizer();
            scoreManager.scorePoints(energizerValue());
            Logger.info("Scored {} points for eating energizer", energizerValue());
        } else {
            scoreManager.scorePoints(pelletValue());
        }
        gateKeeper.registerFoodEaten();
        if (isBonusReached()) {
            activateNextBonus();
            eventLog.bonusIndex = nextBonusIndex;
        }
    }

    @Override
    public void onPacDying() {
        huntingControl.stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true); // TODO how is that realized in Tengen?
        pac.die();
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