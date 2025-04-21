/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.HuntingTimer;
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
 */
public class TengenMsPacMan_GameModel extends GameModel {

    public static Ghost blinky() {
        return new Ghost(GameModel.RED_GHOST_ID, "Blinky");
    }

    public static Ghost pinky() {
        return new Ghost(GameModel.PINK_GHOST_ID, "Pinky");
    }

    public static Ghost inky() {
        return new Ghost(GameModel.CYAN_GHOST_ID, "Inky");
    }

    public static Ghost sue() {
        return new Ghost(GameModel.ORANGE_GHOST_ID, "Sue");
    }

    public static final byte MIN_LEVEL_NUMBER = 1;
    public static final byte MAX_LEVEL_NUMBER = 32;

    static final byte PELLET_VALUE = 10;
    static final byte ENERGIZER_VALUE = 50;

    // See https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PowerPelletTimes.asm
    // Hex value divided by 16 gives the duration in seconds
    static final byte[] POWER_PELLET_TIMES = {
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

    private static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = {2, 4, 8, 16}; // factor * 100 = value

    private static final int DEMO_LEVEL_MIN_DURATION_SEC = 20;

    private static final String HIGH_SCORE_FILENAME = "highscore-ms_pacman_tengen.xml";

    private final TengenMsPacMan_LevelCounter levelCounter;
    private final TengenMsPacMan_HuntingTimer huntingTimer = new TengenMsPacMan_HuntingTimer();
    private final TengenMsPacMan_MapSelector mapSelector;
    private MapCategory mapCategory;
    private Difficulty difficulty;
    private PacBooster pacBooster;
    private boolean boosterActive;
    private int startLevelNumber; // 1-7
    private boolean canStartNewGame;
    private int numContinues;
    private final Steering autopilot = new RuleBasedPacSteering(this);
    private final Steering demoLevelSteering = new RuleBasedPacSteering(this);

    public TengenMsPacMan_GameModel() {
        levelCounter = new TengenMsPacMan_LevelCounter();
        mapSelector = new TengenMsPacMan_MapSelector();
        huntingTimer.setOnPhaseChange(() -> level.ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseASAP));
        lastLevelNumber = MAX_LEVEL_NUMBER;
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

    public void init() {
        scoreManager.setHighScoreFile(new File(HOME_DIR, HIGH_SCORE_FILENAME));
        mapSelector.loadAllMaps(this);
        initialLivesProperty().set(3);
        simulateOverflowBugProperty().set(false); //TODO check if this is correct
        resetForStartingNewGame();
        resetOptions();
    }

    @Override
    public void resetEverything() {
        resetForStartingNewGame();
        resetOptions();
    }

    @Override
    public void resetForStartingNewGame() {
        livesProperty().set(initialLivesProperty().get());
        level = null;
        levelCounter.reset();
        demoLevelProperty().set(false);
        playingProperty().set(false);
        boosterActive = false;
        scoreManager.loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
    }

    @Override
    public void endGame() {
        playingProperty().set(false);
        scoreManager.updateHighScore();
        if (level != null) {
            level.showMessage(GameLevel.Message.GAME_OVER);
        }
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    public void resetOptions() {
        setPacBooster(PacBooster.OFF);
        setDifficulty(Difficulty.NORMAL);
        setMapCategory(MapCategory.ARCADE);
        setStartLevelNumber(1);
        numContinues = 4;
    }

    public boolean hasDefaultOptionValues() {
        return pacBooster == PacBooster.OFF &&
                difficulty == Difficulty.NORMAL &&
                mapCategory == MapCategory.ARCADE &&
                startLevelNumber == 1 &&
                numContinues == 4;
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
        if (number < MIN_LEVEL_NUMBER || number > MAX_LEVEL_NUMBER) {
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
    public long gameOverStateTicks() {
        return isDemoLevel() ? 120 : 420; // TODO how much really?
    }

    @Override
    public void startLevel() {
        super.startLevel();
        // Score runs also in demo level in contrast to Arcade games
        scoreManager.setScoreEnabled(true);
    }

    @Override
    public long pacPowerTicks() {
        if (level == null) {
            return 0;
        }
        int index = level.number() <= 19 ? level.number() - 1 : 18;
        double seconds = POWER_PELLET_TIMES[index] / 16.0;
        return (long) (seconds * 60); // 60 ticks/sec
    }

    @Override
    public long pacPowerFadingTicks() {
        return level != null ? level.numFlashes() * 28L : 0; // TODO check in emulator
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
    public boolean isOver() {
        return livesProperty().get() == 0;
    }

    @Override
    public void startNewGame() {
        resetForStartingNewGame();
        createGameLevel(startLevelNumber);
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

    private void populateLevel(GameLevel level) {
        level.createArcadeHouse(10, 15, 17, 19);

        var pac = new Pac();
        pac.setName("Ms. Pac-Man");
        pac.setGameLevel(level);
        pac.reset();

        var ghosts = new Ghost[] { blinky(), pinky(), inky(), sue() };
        Stream.of(ghosts).forEach(ghost -> {
            ghost.setGameLevel(level);
            ghost.setRevivalPosition(level.ghostPosition(ghost.id()));
            ghost.reset();
        });
        ghosts[RED_GHOST_ID].setRevivalPosition(level.ghostPosition(PINK_GHOST_ID)); // middle house position

        level.setPac(pac);
        level.setGhosts(ghosts);

        //TODO this might not be appropriate for Tengen Ms. Pac-Man
        level.setBonusSymbol(0, computeBonusSymbol(level.number()));
        level.setBonusSymbol(1, computeBonusSymbol(level.number()));
    }

    @Override
    public void buildGameLevel(int levelNumber) {
        WorldMap worldMap = mapSelector.selectWorldMap(mapCategory, levelNumber);

        level = new GameLevel(this, levelNumber, worldMap);
        level.setNumFlashes(5); // TODO check this
        level.setCutSceneNumber(cutSceneNumberAfterLevel(levelNumber));

        populateLevel(level);
        level.pac().setAutopilot(autopilot);
        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
        // Ghosts inside house start at bottom of house instead at middle
        level.ghosts().filter(ghost -> ghost.id() != GameModel.RED_GHOST_ID).forEach(ghost ->
            level.setGhostPosition(ghost.id(), level.ghostPosition(ghost.id()).plus(0, HTS))
        );
        levelCounter.setEnabled(levelNumber < 8);

        activatePacBooster(false); // gets activated in startLevel() if mode is ALWAYS_ON
    }

    @Override
    public void buildDemoLevel() {
        demoLevelProperty().set(true);

        WorldMap worldMap = mapSelector.coloredWorldMap(mapCategory, 1);

        level = new GameLevel(this, 1, worldMap);
        level.setNumFlashes(5); // TODO check this
        level.setCutSceneNumber(0);

        populateLevel(level);

        level.ghosts().forEach(ghost -> ghost.setHuntingBehaviour(this::ghostHuntingBehaviour));
        // ghosts inside house start at floor of house
        level.ghosts().filter(ghost -> ghost.id() != GameModel.RED_GHOST_ID).forEach(ghost ->
                level.setGhostPosition(ghost.id(), level.ghostPosition(ghost.id()).plus(0, HTS))
        );
        levelCounter.setEnabled(true);

        activatePacBooster(false); // gets activated in startLevel() if mode is ALWAYS_ON

        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        level.showMessage(GameLevel.Message.GAME_OVER);
    }

    private int cutSceneNumberAfterLevel(int levelNumber) {
        return switch (levelNumber) {
            case 2 -> 1;
            case 5 -> 2;
            case 9, 13, 17 -> 3;
            case MAX_LEVEL_NUMBER -> 4;
            default -> 0;
        };
    }

    @Override
    public void assignDemoLevelBehavior(Pac pac) {
        pac.setAutopilot(demoLevelSteering);
        pac.setUsingAutopilot(true);
        pac.setImmune(false);
    }

    @Override
    public boolean isPacManKillingIgnored() {
        float levelRunningSeconds = (System.currentTimeMillis() - levelStartTime) / 1000f;
        if (isDemoLevel() && levelRunningSeconds < DEMO_LEVEL_MIN_DURATION_SEC) {
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
    protected void onFoodEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        //TODO does Ms. Pac-Man slow down after eating here too?
        //pac.setRestingTicks(energizer ? 3 : 1);
        if (energizer) {
            onEnergizerEaten();
            scoreManager.scorePoints(ENERGIZER_VALUE);
            Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        } else {
            scoreManager.scorePoints(PELLET_VALUE);
        }
        gateKeeper.registerFoodEaten(level);
        if (isBonusReached()) {
            activateNextBonus();
            eventLog.bonusIndex = level.nextBonusIndex();
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
        eventLog.killedGhosts.add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    @Override
    protected void onGhostReleased(Ghost ghost) {
        // code that is executed when ghost is released from jailhouse
    }

    // TODO clarify what exactly Tengen Ms. Pac-Man does
    private void ghostHuntingBehaviour(Ghost ghost) {
        float speed = ghostAttackSpeed(ghost);
        if (huntingTimer.phaseIndex() == 0 && (ghost.id() == RED_GHOST_ID || ghost.id() == PINK_GHOST_ID)) {
            ghost.roam(speed);
        } else {
            boolean chasing = huntingTimer.huntingPhase() == HuntingTimer.HuntingPhase.CHASING;
            Vector2i targetTile = chasing
                ? chasingTargetTile(ghost.id(), level, simulateOverflowBugProperty().get())
                : level.ghostScatterTile(ghost.id());
            ghost.followTarget(targetTile, speed);
        }
    }
}