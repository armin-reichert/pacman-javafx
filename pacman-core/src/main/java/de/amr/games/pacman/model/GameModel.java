/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.*;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Common part of all game variants.
 *
 * @author Armin Reichert
 */
public abstract class GameModel {

    public static final byte ARCADE_MAP_TILES_X = 28;
    public static final byte ARCADE_MAP_TILES_Y = 36;
    public static final int  ARCADE_MAP_SIZE_X = ARCADE_MAP_TILES_X * TS;
    public static final int  ARCADE_MAP_SIZE_Y = ARCADE_MAP_TILES_Y * TS;

    // Ghost IDs
    public static final byte RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

    /** Game loop frequency, ticks per second. */
    public static final float FPS = 60;

    /** Duration of one tick (in seconds). */
    public static final float SEC_PER_TICK = 1f / FPS;

    /** Movement speed in pixel/sec. */
    public static final float PPS_AT_100_PERCENT = 73.9f; //TODO this should be 75 but that doesn't work yet

    public static final GameLevel[] LEVELS = {
        /* 1*/ new GameLevel( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5, 0),
        /* 2*/ new GameLevel( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5, 1),
        /* 3*/ new GameLevel( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5, 0),
        /* 4*/ new GameLevel( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5, 0),
        /* 5*/ new GameLevel(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5, 2),
        /* 6*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5, 0),
        /* 7*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0),
        /* 8*/ new GameLevel(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5, 0),
        /* 9*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3, 3),
        /*10*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5, 0),
        /*11*/ new GameLevel(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5, 0),
        /*12*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 0),
        /*13*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3, 3),
        /*14*/ new GameLevel(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5, 0),
        /*15*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*16*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*17*/ new GameLevel(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0, 3),
        /*18*/ new GameLevel(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3, 0),
        /*19*/ new GameLevel(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0),
        /*20*/ new GameLevel(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0),
        /*21*/ new GameLevel( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0, 0)
    };

    private static GameLevel level(int levelNumber) {
        return LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)];
    }

    /** Maximum number of coins, as in MAME. */
    public static final byte    MAX_CREDIT = 99;
    public static final byte    POINTS_PELLET = 10;
    public static final byte    POINTS_ENERGIZER = 50;
    public static final short   POINTS_ALL_GHOSTS_IN_LEVEL = 12_000;
    public static final short   EXTRA_LIFE_SCORE = 10_000;
    public static final byte    LEVEL_COUNTER_MAX_SIZE = 7;
    public static final byte    PAC_POWER_FADING_TICKS = 120; // unsure
    public static final byte    BONUS_POINTS_SHOWN_TICKS = 120; // unsure
    public static final byte    RESTING_TICKS_PELLET = 1;
    public static final byte    RESTING_TICKS_ENERGIZER = 3;
    public static final byte    PPS_GHOST_INSIDE_HOUSE = 30; // correct?
    public static final byte    PPS_GHOST_RETURNING_HOME = 120; // correct?
    public static final short[] KILLED_GHOST_VALUES = { 200, 400, 800, 1600 };

    public static byte checkGhostID(byte id) {
        if (id < 0 || id > 3) {
            throw GameException.illegalGhostID(id);
        }
        return id;
    }

    public static int checkLevelNumber(int number) {
        if (number < 1) {
            throw GameException.illegalLevelNumber(number);
        }
        return number;
    }

    protected final File userDir;
    protected final Pulse          blinking = new Pulse(10, Pulse.OFF);
    protected final byte[]         bonusSymbols = new byte[2];
    protected final List<Byte>     levelCounter = new ArrayList<>();
    protected final TickTimer      huntingTimer = new TickTimer("HuntingTimer");
    protected final TickTimer      powerTimer = new TickTimer("PacPowerTimer");
    protected final List<Ghost>    victims = new ArrayList<>();
    protected final Score          score = new Score();
    protected final Score          highScore = new Score();
    protected final GateKeeper     gateKeeper = new GateKeeper();

    protected File                 highScoreFile;
    protected int                  levelNumber; // 1=first level
    protected boolean              demoLevel;
    protected long                 levelStartTime;
    protected boolean              playing;
    protected int                  initialLives;
    protected int                  lives;

    protected byte                 huntingPhaseIndex;
    protected byte                 cruiseElroy;
    protected byte                 numGhostsKilledInLevel;
    protected byte                 nextBonusIndex; // -1=no bonus, 0=first, 1=second

    protected boolean              overflowBug = true;

    protected Pac                  pac;
    protected Ghost[]              ghosts;
    protected Bonus                bonus;
    protected GameWorld            world;

    protected SimulationStepEventLog eventLog;

    protected GameModel(File userDir) {
        this.userDir = checkNotNull(userDir);
    }

    public abstract GameVariant variant();
    public abstract void activateNextBonus();

    protected abstract void buildRegularLevel(int levelNumber);
    protected abstract void buildDemoLevel();
    protected abstract byte computeBonusSymbol();
    protected abstract long huntingTicks(int levelNumber, int phaseIndex);
    protected abstract boolean isPacManKillingIgnoredInDemoLevel();
    protected abstract boolean isBonusReached();
    protected abstract boolean isLevelCounterEnabled();

    protected void clearLevel() {
        levelNumber = 0;
        levelStartTime = 0;
        huntingPhaseIndex = 0;
        huntingTimer.resetIndefinitely();
        numGhostsKilledInLevel = 0;
        cruiseElroy = 0;
        bonus = null;
        nextBonusIndex = -1;
        Arrays.fill(bonusSymbols, (byte)-1);
        pac = null;
        ghosts = null;
        world = null;
        blinking.stop();
        blinking.reset();
    }

    protected void setWorldAndCreatePopulation(GameWorld world) {
        this.world = world;

        bonusSymbols[0] = computeBonusSymbol();
        bonusSymbols[1] = computeBonusSymbol();

        pac = new Pac();
        pac.setWorld(world);
        pac.setName("Pac-Man");
        pac.reset();
        pac.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);

        ghosts = new Ghost[] {
            new Ghost(RED_GHOST, world),
            new Ghost(PINK_GHOST, world),
            new Ghost(CYAN_GHOST, world),
            new Ghost(ORANGE_GHOST, world)
        };

        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
            ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME * SEC_PER_TICK);
            ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE * SEC_PER_TICK);
            ghost.setRevivalPosition(world.ghostPosition(ghost.id()));
        });
        ghosts[RED_GHOST].setRevivalPosition(world.ghostPosition(PINK_GHOST)); // middle house position
    }

    protected void setCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

    public Pac pac() {
        return pac;
    }

    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts[id];
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        checkNotNull(states);
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    public List<Ghost> victims() {
        return victims;
    }

    public Pulse blinking() {
        return blinking;
    }

    public void startHuntingPhase(int phaseIndex) {
        huntingPhaseIndex = checkHuntingPhaseIndex(phaseIndex);
        huntingTimer.reset(huntingTicks(levelNumber, huntingPhaseIndex));
        huntingTimer.start();
        String phaseName = isScatterPhase(huntingPhaseIndex) ? "Scattering" : "Chasing";
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            huntingPhaseIndex, phaseName,
            huntingTimer.duration(), (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    public boolean isScatterPhase(int phaseIndex) {
        return isEven(phaseIndex);
    }

    public boolean isChasingPhase(int phaseIndex) {
        return isOdd(phaseIndex);
    }

    public Optional<Integer> scatterPhase() {
        return isScatterPhase(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    public Optional<Integer> chasingPhase() {
        return isChasingPhase(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    public int levelNumber() {
        return levelNumber;
    }

    public Optional<GameLevel> level() {
        if (levelNumber == 0) {
            return Optional.empty();
        }
        return Optional.of(level(levelNumber));
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    public int intermissionNumber(int levelNumber) {
        return level(levelNumber).intermissionNumber();
    }

    public byte cruiseElroyState() {
        return cruiseElroy;
    }

    public void reset() {
        playing = false;
        lives = initialLives;
        clearLevel();
        score.reset();
    }

    public void createLevel(int levelNumber) {
        clearLevel();
        demoLevel = false;
        buildRegularLevel(levelNumber);
        score.setLevelNumber(levelNumber);
        Logger.info("Level {} created", levelNumber);
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    public void createDemoLevel() {
        clearLevel();
        demoLevel = true;
        buildDemoLevel();
        Logger.info("Demo Level created");
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    public void startLevel() {
        if (levelNumber == 1) {
            levelCounter.clear();
        }
        if (isLevelCounterEnabled()) {
            levelCounter.add(bonusSymbols[0]);
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SIZE) {
                levelCounter.remove(0);
            }
        }
        gateKeeper.init(levelNumber);
        letsGetReadyToRumble();
        levelStartTime = System.currentTimeMillis();
        Logger.info("{} started ({})", demoLevel ? "Demo Level" : "Level " + levelNumber, variant());
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    public void removeWorld() {
        world = null;
    }

    /**
     * Sets each guy to his start position and resets him to his initial state. Note that they are all invisible
     * initially!
     */
    public void letsGetReadyToRumble() {
        pac.reset(); // invisible!
        pac.setPosition(world.pacPosition());
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.selectAnimation(Pac.ANIM_MUNCHING);
        pac.animations().ifPresent(Animations::resetSelected);
        ghosts().forEach(ghost -> {
            ghost.reset(); // invisible!
            ghost.setPosition(world.ghostPosition(ghost.id()));
            ghost.setMoveAndWishDir(world.ghostDirection(ghost.id()));
            ghost.setState(LOCKED);
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
        powerTimer.resetIndefinitely();
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    public void showGuys() {
        pac.setVisible(true);
        ghosts().forEach(ghost -> ghost.setVisible(true));
    }

    public void hideGuys() {
        pac.setVisible(false);
        ghosts().forEach(ghost -> ghost.setVisible(false));
    }

    public SimulationStepEventLog eventLog() {
        return eventLog;
    }

    public void clearEventLog() {
        eventLog = new SimulationStepEventLog();
    }

    protected Vector2i scatterTarget(Ghost ghost) {
        return world.ghostScatterTile(ghost.id());
    }

    /*
     * Regarding the overflow bug, see http://www.donhodges.com/pacman_pinky_explanation.htm
     */
    protected Vector2i chasingTarget(Ghost ghost) {
        return switch (ghost.id()) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAhead(4, overflowBug);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAhead(2, overflowBug).scaled(2).minus(ghost(RED_GHOST).tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghost.tile().euclideanDistance(pac.tile()) < 8 ? scatterTarget(ghost) : pac.tile();
            default -> throw GameException.illegalGhostID(ghost.id());
        };
    }

    protected byte huntingSpeedPct(Ghost ghost) {
        GameLevel level = level(levelNumber);
        if (world.isTunnel(ghost.tile())) {
            return level.ghostSpeedTunnelPct();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 1) {
            return level.elroy1SpeedPct();
        }
        if (ghost.id() == RED_GHOST && cruiseElroy == 2) {
            return level.elroy2SpeedPct();
        }
        return level.ghostSpeedPct();
    }

    public GameWorld world() {
        return world;
    }

    public int initialLives() {
        return initialLives;
    }

    public void setInitialLives(int lives) {
        initialLives = lives;
    }

    public int lives() {
        return lives;
    }

    public void addLives(int deltaLives) {
        lives += deltaLives;
    }

    public void loseLife() {
        if (lives == 0) {
            Logger.error("No life left to lose :-(");
            return;
        }
        --lives;
    }

    public List<Byte> levelCounter() {
        return levelCounter;
    }

    public Score score() {
        return score;
    }

    public void scorePoints(int points) {
        if (demoLevel) {
            return;
        }
        int oldScore = score.points();
        int newScore = oldScore + points;
        score.setPoints(newScore);
        if (newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
            highScore.setDate(LocalDate.now());
        }
        if (oldScore < EXTRA_LIFE_SCORE && newScore >= EXTRA_LIFE_SCORE) {
            addLives(1);
            publishGameEvent(GameEventType.EXTRA_LIFE_WON);
        }
    }

    public Score highScore() {
        return highScore;
    }

    public void loadHighScore() {
        highScore.read(highScoreFile);
        Logger.info("Highscore loaded. File: '{}', {} points, level {}",
            highScoreFile, highScore.points(), highScore.levelNumber());
    }

    public void updateHighScore() {
        var oldHighScore = new Score();
        oldHighScore.read(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            highScore.save(highScoreFile, String.format("%s High Score", variant().name()));
        }
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void onPacDying() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and set to zero");
        gateKeeper.resetCounterAndSetEnabled(true);
        setCruiseElroyEnabled(false);
        pac.die();
    }

    public void onLevelCompleted() {
        blinking.setStartPhase(Pulse.OFF);
        blinking.reset();
        pac.freeze();
        bonus().ifPresent(Bonus::setInactive);
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and reset to zero");
        Logger.trace("Game level {} completed.", levelNumber);
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public boolean isLevelComplete() {
        return world.uneatenFoodCount() == 0;
    }

    public boolean isPacManKilled() {
        return eventLog.pacKilled;
    }

    public boolean areGhostsKilled() {
        return !eventLog.killedGhosts.isEmpty();
    }

    public void doHuntingStep() {
        blinking.tick();
        checkForFood();
        unlockGhosts();
        ghosts().forEach(ghost -> ghost.update(this));
        pac.update(this);
        if (bonus != null) updateBonus();
        updatePacPower();
        updateHuntingTimer();
        ghosts(FRIGHTENED).filter(pac::sameTile).forEach(this::killGhost);
        eventLog.pacKilled = checkPacKilled(pac.isImmune());
    }

    private boolean checkPacKilled(boolean pacImmune) {
        boolean pacMeetsKiller = ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
        if (demoLevel) {
            return pacMeetsKiller && !isPacManKillingIgnoredInDemoLevel();
        } else {
            return pacMeetsKiller && !pacImmune;
        }
    }

    private void checkForFood() {
        final Vector2i pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            eventLog.foodFoundTile = pacTile;
            pac.onStarvingEnd();
            if (world.isEnergizerPosition(pacTile)) {
                eventLog.energizerFound = true;
                pac.setRestingTicks(RESTING_TICKS_ENERGIZER);
                victims.clear();
                scorePoints(POINTS_ENERGIZER);
                Logger.info("Scored {} points for eating energizer", POINTS_ENERGIZER);
                if (level(levelNumber).pacPowerSeconds() > 0) {
                    eventLog.pacGetsPower = true;
                    huntingTimer.stop();
                    Logger.info("Hunting timer stopped");
                    int seconds = level(levelNumber).pacPowerSeconds();
                    powerTimer.restartSeconds(seconds);
                    Logger.info("Power timer restarted to {} seconds", seconds);
                    // TODO do already frightened ghosts reverse too?
                    ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
                    ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
                    publishGameEvent(GameEventType.PAC_GETS_POWER);
                }
            } else {
                pac.setRestingTicks(RESTING_TICKS_PELLET);
                scorePoints(POINTS_PELLET);
            }
            gateKeeper.onPelletOrEnergizerEaten(this);
            world.eatFoodAt(pacTile);
            if (world.uneatenFoodCount() == level(levelNumber).elroy1DotsLeft()) {
                cruiseElroy = 1;
            } else if (world.uneatenFoodCount() == level(levelNumber).elroy2DotsLeft()) {
                cruiseElroy = 2;
            }
            if (isBonusReached()) {
                activateNextBonus();
                eventLog.bonusIndex = nextBonusIndex;
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    private void updatePacPower() {
        powerTimer.tick();
        if (powerTimer.remaining() == PAC_POWER_FADING_TICKS) {
            eventLog.pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            victims.clear();
            huntingTimer.start();
            Logger.info("Hunting timer started");
            ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean isPowerFading() {
        return powerTimer.isRunning() && powerTimer.remaining() <= PAC_POWER_FADING_TICKS;
    }

    public boolean isPowerFadingStarting() {
        return powerTimer.isRunning() && powerTimer.remaining() == PAC_POWER_FADING_TICKS
            || powerTimer.duration() < PAC_POWER_FADING_TICKS && powerTimer.currentTick() == 1;
    }

    private void updateBonus() {
        if (bonus.state() == Bonus.STATE_EDIBLE && pac.sameTile(bonus.entity())) {
            bonus.setEaten(BONUS_POINTS_SHOWN_TICKS);
            scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog.bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }

    private void updateHuntingTimer( ) {
        huntingTimer.tick();
        if (huntingTimer.hasExpired()) {
            Logger.info("Hunting timer expired, tick={}", huntingTimer.currentTick());
            startHuntingPhase(huntingPhaseIndex + 1);
            ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
        }
    }

    private void unlockGhosts() {
        Ghost blinky = ghost(RED_GHOST);
        if (blinky.inState(LOCKED)) {
            if (blinky.insideHouse()) {
                blinky.setMoveAndWishDir(Direction.UP);
                blinky.setState(LEAVING_HOUSE);
            } else {
                blinky.setMoveAndWishDir(LEFT);
                blinky.setState(HUNTING_PAC);
            }
        }
        // Ghosts in order PINK, CYAN, ORANGE!
        Ghost prisoner = ghosts(LOCKED).findFirst().orElse(null);
        if (prisoner != null) {
            String releaseInfo = gateKeeper.checkReleaseOf(this, prisoner);
            if (releaseInfo != null) {
                eventLog.releasedGhost = prisoner;
                eventLog.ghostReleaseInfo = releaseInfo;
                prisoner.setMoveAndWishDir(Direction.UP);
                prisoner.setState(LEAVING_HOUSE);
                if (prisoner.id() == ORANGE_GHOST && cruiseElroyState() < 0) {
                    Logger.trace("Re-enable cruise elroy mode because {} exits house:", prisoner.name());
                    setCruiseElroyEnabled(true);
                }
            }
        }
    }

    public void killGhost(Ghost ghost) {
        eventLog.killedGhosts.add(ghost);
        int killedSoFar = victims.size();
        int points = KILLED_GHOST_VALUES[killedSoFar];
        ghost.eaten(killedSoFar);
        scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        numGhostsKilledInLevel += 1;
        if (numGhostsKilledInLevel == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_IN_LEVEL;
            scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, levelNumber);
        }
        victims.add(ghost);
    }

    // Game Event Support

    private final List<GameEventListener> gameEventListeners = new ArrayList<>();

    public void addGameEventListener(GameEventListener listener) {
        checkNotNull(listener);
        gameEventListeners.add(listener);
    }

    public void publishGameEvent(GameEvent event) {
        checkNotNull(event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    public void publishGameEvent(GameEventType type) {
        checkNotNull(type);
        publishGameEvent(new GameEvent(type, this));
    }

    public void publishGameEvent(GameEventType type, Vector2i tile) {
        checkNotNull(type);
        publishGameEvent(new GameEvent(type, this, tile));
    }

    // Coins

    private int numCoins = 0;

    /**
     * @return number of coins inserted.
     */
    public int credit() {
        return numCoins;
    }

    public void setNumCoins(int numCoins) {
        if (numCoins >= 0 && numCoins <= GameModel.MAX_CREDIT) {
            this.numCoins = numCoins;
        }
    }

    public boolean insertCoin() {
        if (numCoins < GameModel.MAX_CREDIT) {
            ++numCoins;
            return true;
        }
        return false;
    }

    public void consumeCoin() {
        if (numCoins > 0) {
            --numCoins;
        }
    }

    public boolean hasCredit() {
        return numCoins > 0;
    }
}