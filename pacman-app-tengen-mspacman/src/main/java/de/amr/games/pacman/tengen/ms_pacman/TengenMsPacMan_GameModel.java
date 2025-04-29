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
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_MS_PACMAN_BOOSTER;
import static de.amr.games.pacman.model.actors.GhostState.*;
import static de.amr.games.pacman.tengen.ms_pacman.SpeedConfiguration.*;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @author Armin Reichert
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends GameModel {

    private static final byte MIN_LEVEL_NUMBER = 1;
    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

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

    private final TengenMsPacMan_LevelCounter levelCounter;
    private final TengenMsPacMan_MapSelector mapSelector;
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
        highScoreFile = new File(HOME_DIR, "highscore-ms_pacman_tengen.xml");
        levelCounter = new TengenMsPacMan_LevelCounter();
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
        initialLivesProperty().set(3);
        resetEverything();
    }

    @Override
    public void resetEverything() {
        resetForStartingNewGame();
        setPacBooster(PacBooster.OFF);
        setDifficulty(Difficulty.NORMAL);
        setMapCategory(MapCategory.ARCADE);
        setStartLevelNumber(1);
        numContinues = 4;
    }

    @Override
    public void resetForStartingNewGame() {
        livesProperty().set(initialLivesProperty().get());
        level = null;
        levelCounter.reset();
        playingProperty().set(false);
        boosterActive = false;
        loadHighScore();
        resetScore();
        gateKeeper.reset();
    }

    @Override
    public void endGame() {
        playingProperty().set(false);
        updateHighScore();
        if (level != null) {
            level.showMessage(GameLevel.Message.GAME_OVER);
        }
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public int lastLevelNumber() {
        return 32;
    }

    @Override
    protected Optional<GateKeeper> gateKeeper() {
        return Optional.of(gateKeeper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LevelCounter> T levelCounter() {
        return (T) levelCounter;
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    public boolean optionsHaveDefaultValues() {
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
            extraLifeScores = List.of(10_000, 970_000, 980_000, 990_000);
        } else {
            extraLifeScores = List.of(10_000, 50_000, 100_000, 300_000);
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
        if (number < MIN_LEVEL_NUMBER || number > lastLevelNumber()) {
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
        super.startLevel();
        // Score runs also in demo level in contrast to Arcade games
        setScoreEnabled(true);
    }

    @Override
    public long pacPowerTicks() {
        if (level == null) return 0;
        int index = level.number() <= 19 ? level.number() - 1 : 18;
        double seconds = POWER_PELLET_TIMES[index] / 16.0;
        return (long) (seconds * 60); // 60 ticks/sec
    }

    @Override
    public long pacPowerFadingTicks() {
        return level != null ? level.data().numFlashes() * 28L : 0; // TODO check in emulator
    }

    @Override
    public long pacDyingTicks() {
        return 300;
    }

    @Override
    public float pacNormalSpeed() {
        return level.pac() != null ? level.pac().baseSpeed() : 0;
    }

    @Override
    public float pacPowerSpeed() {
        //TODO is this correct?
        return 1.1f * level.pac().baseSpeed();
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        if (level.isTunnel(ghost.tile())) {
            return ghostTunnelSpeed(ghost);
        }
        float speed = ghost.baseSpeed();
        float increase = ghostSpeedIncreaseByFoodRemaining(this);
        if (increase > 0) {
            speed += increase;
            Logger.debug("Ghost speed increased by {} units to {0.00} px/tick for {}", increase, speed, ghost.name());
        }
        return speed;
    }

    @Override
    public float ghostSpeedInsideHouse(Ghost ghost) { return 0.5f; }

    @Override
    public float ghostSpeedReturningToHouse(Ghost ghost) { return 2; }

    @Override
    public float ghostFrightenedSpeed(Ghost ghost) { return 0.5f * ghost.baseSpeed(); } //TODO is this correct?

    @Override
    public float ghostTunnelSpeed(Ghost ghost) { return 0.4f * ghost.baseSpeed(); } //TODO is this correct?

    @Override
    public boolean isOver() { return livesProperty().get() == 0; }

    @Override
    public void startNewGame() {
        resetForStartingNewGame();
        createLevel(startLevelNumber, createLevelData(startLevelNumber));
        levelCounter.resetStartingFromLevel(startLevelNumber);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.GAME_STARTED);
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        float pacBaseSpeed = pacBaseSpeedInLevel(levelNumber) + pacDifficultySpeedDelta(difficulty);
        level.pac().setBaseSpeed(pacBaseSpeed);
        if (pacBooster == PacBooster.ALWAYS_ON) {
            activatePacBooster(true);
        }
        level.ghosts().forEach(ghost ->
            ghost.setBaseSpeed(ghostBaseSpeedInLevel(levelNumber)
                + ghostDifficultySpeedDelta(difficulty)
                + ghostIDSpeedDelta(ghost.id()))
        );
        Logger.info("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
        level.ghosts().forEach(ghost -> Logger.info("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));
    }

    @Override
    protected void initActorAnimationState() {
        level.pac().selectAnimation(boosterActive
            ? ANIM_MS_PACMAN_BOOSTER : ActorAnimations.ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void activatePacBooster(boolean active) {
        if (boosterActive != active) {
            boosterActive = active;
            float speed = pacBaseSpeedInLevel(level.number()) + pacDifficultySpeedDelta(difficulty);
            if (boosterActive) {
                speed += pacBoosterSpeedDelta();
            }
            level.pac().setBaseSpeed(speed);
            level.pac().selectAnimation(boosterActive
                ? ANIM_MS_PACMAN_BOOSTER : ActorAnimations.ANIM_PAC_MUNCHING);
        }
    }

    @Override
    public void buildLevel(int levelNumber, LevelData data) {
        WorldMap worldMap = mapSelector.selectWorldMap(mapCategory, levelNumber);
        level = new GameLevel(this, levelNumber, data, worldMap);
        level.setCutSceneNumber(switch (levelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            default -> levelNumber == lastLevelNumber() ? 4 : 0;
        });
        level.setGameOverStateTicks(420);

        level.createArcadeHouse(10, 15, 17, 19);

        var pac = new Pac();
        pac.setName("Ms. Pac-Man");
        pac.setGameLevel(level);
        pac.reset();
        pac.setAutopilot(autopilot);

        var ghosts = new Ghost[] {
            new Ghost(RED_GHOST_ID, "Blinky"),
            new Ghost(PINK_GHOST_ID, "Pinky"),
            new Ghost(CYAN_GHOST_ID, "Inky"),
            new Ghost(ORANGE_GHOST_ID, "Sue")
        };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setGameLevel(level);
            ghost.setRevivalPosition(level.ghostStartPosition(ghost.id()));
            ghost.reset();
            ghost.setHuntingBehaviour(this::ghostHuntingBehaviour);
        });

        // Ghosts inside house start at bottom of house instead at middle as marked in map
        Stream.of(ghosts).filter(ghost -> ghost.id() != RED_GHOST_ID).forEach(ghost ->
            level.setGhostStartPosition(ghost.id(), level.ghostStartPosition(ghost.id()).plus(0, HTS))
        );
        ghosts[RED_GHOST_ID].setRevivalPosition(level.ghostStartPosition(PINK_GHOST_ID)); // middle house position

        level.setPac(pac);
        level.setGhosts(ghosts);

        //TODO this might not be appropriate for Tengen Ms. Pac-Man
        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));

        levelCounter.setEnabled(levelNumber < 8);
        activatePacBooster(false); // gets activated in startLevel() if mode is ALWAYS_ON
    }

    @Override
    public LevelData createLevelData(int levelNumber) {
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
    public void buildDemoLevel() {
        buildLevel(1, createLevelData(1));
        level.setDemoLevel(true);
        level.setGameOverStateTicks(120);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
    }

    @Override
    public void assignDemoLevelBehavior(Pac pac) {
        pac.setAutopilot(demoLevelSteering);
        pac.setUsingAutopilot(true);
        pac.setImmune(false);
    }

    @Override
    public boolean isPacManKillingIgnored() {
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

    @Override
    public byte computeBonusSymbol(int levelNumber) {
        //TODO: I have no idea yet how Tengen does this
        byte maxBonus = mapCategory == MapCategory.STRANGE ? BONUS_FLOWER : BONUS_BANANA;
        if (levelNumber - 1 <= maxBonus) {
            return (byte) (levelNumber - 1);
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
        level.selectNextBonus();

        boolean leftToRight = THE_RNG.nextBoolean();
        Vector2i houseEntry = tileAt(level.houseEntryPosition());
        Vector2i houseEntryOpposite = houseEntry.plus(0, level.houseSizeInTiles().y() + 1);
        List<Portal> portals = level.portals().toList();
        if (portals.isEmpty()) {
            return; // there should be no mazes without portal but who knows?
        }
        Portal entryPortal = portals.get(THE_RNG.nextInt(portals.size()));
        Portal exitPortal  = portals.get(THE_RNG.nextInt(portals.size()));
        List<Waypoint> route = Stream.of(
            leftToRight ? entryPortal.leftTunnelEnd() : entryPortal.rightTunnelEnd(),
            houseEntry,
            houseEntryOpposite,
            houseEntry,
            leftToRight ? exitPortal.rightTunnelEnd().plus(1, 0) : exitPortal.leftTunnelEnd().minus(1, 0)
        ).map(Waypoint::new).toList();

        byte symbol = level.bonusSymbol(level.nextBonusIndex());
        var movingBonus = new MovingBonus(level, symbol, BONUS_VALUE_FACTORS[symbol] * 100);
        movingBonus.setRoute(route, leftToRight);
        movingBonus.setBaseSpeed(1f); // TODO how fast is the bonus really moving?
        Logger.debug("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        level.setBonus(movingBonus);
        movingBonus.setEdible(TickTimer.INDEFINITE);
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.BONUS_ACTIVATED, movingBonus.actor().tile());
    }

    @Override
    protected void onPelletEaten(Vector2i tile) {
        //TODO does Ms. Pac-Man slow down after eating as in Arcade game?
        scorePoints(PELLET_VALUE);
    }

    @Override
    protected void onEnergizerEaten(Vector2i tile) {
        //TODO does Ms. Pac-Man slow down after eating as in Arcade game?
        scorePoints(ENERGIZER_VALUE);
        Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        level.victims().clear();
        long powerTicks = pacPowerTicks();
        if (powerTicks > 0) {
            huntingTimer().stop();
            Logger.info("Hunting Pac-Man stopped as he got power");
            level.pac().powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / TICKS_PER_SECOND);
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
    public void killGhost(Ghost ghost) {
        THE_SIMULATION_STEP.killedGhosts().add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    // TODO clarify what exactly Tengen Ms. Pac-Man does
    private void ghostHuntingBehaviour(Ghost ghost) {
        float speed = ghostAttackSpeed(ghost);
        if (huntingTimer.phaseIndex() == 0 && (ghost.id() == RED_GHOST_ID || ghost.id() == PINK_GHOST_ID)) {
            ghost.roam(speed);
        } else {
            boolean chasing = huntingTimer.phase() == HuntingPhase.CHASING;
            Vector2i targetTile = chasing
                ? chasingTargetTile(ghost.id(), level, false)
                : level.ghostScatterTile(ghost.id());
            ghost.followTarget(targetTile, speed);
        }
    }
}