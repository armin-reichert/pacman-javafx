/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.checkGhostID;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Common part of all game variants.
 *
 * @author Armin Reichert
 */
public abstract class AbstractPacManGame implements GameModel {

    static final GameLevel[] LEVELS = {
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

    static GameLevel level(int levelNumber) {
        return LEVELS[Math.min(levelNumber - 1, LEVELS.length - 1)];
    }

    static final byte    POINTS_PELLET = 10;
    static final byte    POINTS_ENERGIZER = 50;
    static final short   POINTS_ALL_GHOSTS_IN_LEVEL = 12_000;
    static final short   EXTRA_LIFE_SCORE = 10_000;
    static final byte    LEVEL_COUNTER_MAX_SYMBOLS = 7;
    static final byte    PAC_POWER_FADING_TICKS = 120; // unsure
    static final byte    BONUS_POINTS_SHOWN_TICKS = 120; // unsure
    static final byte    RESTING_TICKS_PELLET = 1;
    static final byte    RESTING_TICKS_ENERGIZER = 3;
    static final byte    PPS_GHOST_INSIDE_HOUSE = 30; // correct?
    static final byte    PPS_GHOST_RETURNING_HOME = 120; // correct?
    static final short[] KILLED_GHOST_VALUES = { 200, 400, 800, 1600 };

    final Pulse          blinking = new Pulse(10, Pulse.OFF);
    final byte[]         bonusSymbols = new byte[2];
    final List<Byte>     levelCounter = new ArrayList<>();
    final TickTimer      huntingTimer = new TickTimer("HuntingTimer");
    final TickTimer      powerTimer = new TickTimer("PacPowerTimer");
    final List<Ghost>    victims = new ArrayList<>();
    final Score          score = new Score();
    final Score          highScore = new Score();
    final GateKeeper     gateKeeper = new GateKeeper();

    String               highScoreFileName;
    int                  levelNumber; // 1=first level
    boolean              demoLevel;
    long                 levelStartTime;
    boolean              playing;
    int                  initialLives;
    int                  lives;

    byte                 huntingPhaseIndex;
    byte                 cruiseElroy;
    byte                 numGhostsKilledInLevel;
    byte                 nextBonusIndex; // -1=no bonus, 0=first, 1=second

    Pac pac;
    Ghost[]              ghosts;
    Bonus bonus;
    World                world;

    SimulationStepEventLog eventLog;

    abstract void buildRegularLevel(int levelNumber);

    abstract void buildDemoLevel();

    abstract byte computeBonusSymbol();

    abstract long huntingTicks(int levelNumber, int phaseIndex);

    abstract boolean isPacManKillingIgnoredInDemoLevel();

    abstract boolean isBonusReached();

    abstract void updateLevelCounter();

    House createArcadeHouse() {
        var house = new House();
        house.setSize(v2i(8, 5));
        house.setDoor(new Door(v2i(13, 15), v2i(14, 15)));
        return house;
    }

    void clearLevel() {
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

    WorldMap loadMap(String path) {
        URL mapURL = getClass().getResource(path);
        if (mapURL != null) {
            try {
                var map = new WorldMap(mapURL);
                Logger.info("Map loaded from URL {}", mapURL);
                return map;
            }
            catch (IOException x) {
                Logger.error("Error loading world map from URL '{}'", mapURL);
                return null;
            }
        }
        Logger.error("Error loading web map from resource path '{}'", path);
        return null;
    }

    void setWorldAndCreatePopulation(World world) {
        this.world = world;

        bonusSymbols[0] = computeBonusSymbol();
        bonusSymbols[1] = computeBonusSymbol();

        pac = new Pac(world);
        pac.setName("Pac-Man");
        pac.reset();
        pac.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);

        ghosts = new Ghost[] {
            new Ghost(RED_GHOST,    world),
            new Ghost(PINK_GHOST,   world),
            new Ghost(CYAN_GHOST,   world),
            new Ghost(ORANGE_GHOST, world)
        };
        ghosts[RED_GHOST]   .setName("Blinky");
        ghosts[PINK_GHOST]  .setName("Pinky");
        ghosts[CYAN_GHOST]  .setName("Inky");
        ghosts[ORANGE_GHOST].setName("Clyde");

        ghosts[RED_GHOST]   .setRevivalPosition(world.ghostPosition(PINK_GHOST)); // !
        ghosts[PINK_GHOST]  .setRevivalPosition(world.ghostPosition(PINK_GHOST));
        ghosts[CYAN_GHOST]  .setRevivalPosition(world.ghostPosition(CYAN_GHOST));
        ghosts[ORANGE_GHOST].setRevivalPosition(world.ghostPosition(ORANGE_GHOST));

        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setBaseSpeed(PPS_AT_100_PERCENT * SEC_PER_TICK);
            ghost.setSpeedReturningHome(PPS_GHOST_RETURNING_HOME * SEC_PER_TICK);
            ghost.setSpeedInsideHouse(PPS_GHOST_INSIDE_HOUSE * SEC_PER_TICK);
        });
    }

    void setCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

    @Override
    public Pac pac() {
        return pac;
    }

    @Override
    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts[id];
    }

    @Override
    public Stream<Ghost> ghosts(GhostState... states) {
        checkNotNull(states);
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    @Override
    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    @Override
    public TickTimer powerTimer() {
        return powerTimer;
    }

    @Override
    public List<Ghost> victims() {
        return victims;
    }

    @Override
    public Pulse blinking() {
        return blinking;
    }

    @Override
    public void startHuntingPhase(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + phaseIndex);
        }
        huntingPhaseIndex = (byte) phaseIndex;
        huntingTimer.reset(huntingTicks(levelNumber, huntingPhaseIndex));
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            huntingPhaseIndex, currentHuntingPhaseName(),
            huntingTimer.duration(), (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }
    @Override
    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    @Override
    public Optional<Integer> scatterPhase() {
        return isEven(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    @Override
    public Optional<Integer> chasingPhase() {
        return isOdd(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    @Override
    public String currentHuntingPhaseName() {
        return isEven(huntingPhaseIndex) ? "Scattering" : "Chasing";
    }

    @Override
    public int levelNumber() {
        return levelNumber;
    }

    @Override
    public Optional<GameLevel> level() {
        if (levelNumber == 0) {
            return Optional.empty();
        }
        return Optional.of(level(levelNumber));
    }

    @Override
    public boolean isDemoLevel() {
        return demoLevel;
    }

    @Override
    public int intermissionNumberAfterLevel(int levelNumber) {
        return level(levelNumber).intermissionNumber();
    }

    @Override
    public byte cruiseElroyState() {
        return cruiseElroy;
    }

    @Override
    public void reset() {
        playing = false;
        lives = initialLives;
        clearLevel();
        score.reset();
    }

    @Override
    public void createLevel(int levelNumber) {
        clearLevel();
        demoLevel = false;
        buildRegularLevel(levelNumber);
        score.setLevelNumber(levelNumber);
        Logger.info("Level {} created", levelNumber);
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void createDemoLevel() {
        clearLevel();
        demoLevel = true;
        buildDemoLevel();
        Logger.info("Demo Level created");
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    @Override
    public void startLevel() {
        updateLevelCounter();
        gateKeeper.init(levelNumber);
        letsGetReadyToRumble();
        levelStartTime = System.currentTimeMillis();
        Logger.info("{} started ({})", demoLevel ? "Demo Level" : "Level " + levelNumber, this);
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    @Override
    public void letsGetReadyToRumble() {
        pac.reset();
        pac.setPosition(world.pacPosition());
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.selectAnimation(Pac.ANIM_MUNCHING);
        pac.animations().ifPresent(Animations::resetSelected);
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(world.ghostPosition(ghost.id()));
            ghost.setMoveAndWishDir(world.ghostDirection(ghost.id()));
            ghost.setState(LOCKED);
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    @Override
    public void makeGuysVisible(boolean visible) {
        pac.setVisible(visible);
        ghosts().forEach(ghost -> ghost.setVisible(visible));
    }

    @Override
    public SimulationStepEventLog eventLog() {
        return eventLog;
    }

    @Override
    public void clearEventLog() {
        eventLog = new SimulationStepEventLog();
    }

    Vector2i scatterTarget(Ghost ghost) {
        return world.ghostScatterTile(ghost.id());
    }

    Vector2i chasingTarget(Ghost ghost) {
        return switch (ghost.id()) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAheadWithOverflowBug(4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAheadWithOverflowBug(2).scaled(2).minus(ghost(RED_GHOST).tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghost.tile().euclideanDistance(pac.tile()) < 8 ? scatterTarget(ghost) : pac.tile();
            default -> throw new IllegalGhostIDException(ghost.id());
        };
    }

    byte huntingSpeedPct(Ghost ghost) {
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

    @Override
    public World world() {
        return world;
    }

    @Override
    public int initialLives() {
        return initialLives;
    }

    @Override
    public void setInitialLives(int lives) {
        initialLives = lives;
    }

    @Override
    public int lives() {
        return lives;
    }

    @Override
    public void addLives(int deltaLives) {
        lives += deltaLives;
    }

    @Override
    public void loseLife() {
        if (lives == 0) {
            Logger.error("No life left to lose :-(");
            return;
        }
        --lives;
    }

    @Override
    public List<Byte> levelCounter() {
        return levelCounter;
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
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

    @Override
    public Score highScore() {
        return highScore;
    }

    public void loadHighScore() {
        File file = new File(System.getProperty("user.home"), highScoreFileName);
        highScore.loadFromFile(file);
        Logger.info("Highscore loaded. File: '{}', {} points, level {}",
            file, highScore.points(), highScore.levelNumber());
    }

    @Override
    public void updateHighScore() {
        File file = new File(System.getProperty("user.home"), highScoreFileName);
        var oldHighScore = new Score();
        oldHighScore.loadFromFile(file);
        if (highScore.points() > oldHighScore.points()) {
            highScore.saveToFile(file, String.format("%s High Score", variant().name()));
        }
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
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

    @Override
    public void onLevelCompleted() {
        blinking.setStartPhase(Pulse.OFF);
        blinking.reset();
        pac.freeze();
        ghosts().forEach(Ghost::hide);
        bonus().ifPresent(Bonus::setInactive);
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        powerTimer.stop();
        powerTimer.reset(0);
        Logger.info("Power timer stopped and reset to zero");
        Logger.trace("Game level {} completed.", levelNumber);
    }

    @Override
    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    @Override
    public boolean isLevelComplete() {
        return world.uneatenFoodCount() == 0;
    }

    @Override
    public boolean isPacManKilled() {
        return eventLog.pacKilled;
    }

    @Override
    public boolean areGhostsKilled() {
        return !eventLog.killedGhosts.isEmpty();
    }

    @Override
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
        eventLog.pacKilled = checkPacKilled();
    }

    boolean checkPacKilled() {
        boolean pacMeetsKiller = ghosts(HUNTING_PAC).anyMatch(pac::sameTile);
        if (demoLevel) {
            return pacMeetsKiller && !isPacManKillingIgnoredInDemoLevel();
        } else {
            return pacMeetsKiller && !GameController.it().isPacImmune();
        }
    }

    void checkForFood() {
        final Vector2i pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            eventLog.foodFoundTile = pacTile;
            pac.onStarvingEnd();
            if (world.isEnergizerTile(pacTile)) {
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
                createNextBonus();
                eventLog.bonusIndex = nextBonusIndex;
            }
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, pacTile);
        } else {
            pac.starve();
        }
    }

    void updatePacPower() {
        powerTimer.advance();
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

    @Override
    public boolean isPowerFading() {
        return powerTimer.isRunning() && powerTimer.remaining() <= PAC_POWER_FADING_TICKS;
    }

    @Override
    public boolean isPowerFadingStarting() {
        return powerTimer.isRunning() && powerTimer.remaining() == PAC_POWER_FADING_TICKS
            || powerTimer.duration() < PAC_POWER_FADING_TICKS && powerTimer.tick() == 1;
    }

    void updateBonus() {
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

    void updateHuntingTimer( ) {
        huntingTimer.advance();
        if (huntingTimer.hasExpired()) {
            Logger.info("Hunting timer expired, tick={}", huntingTimer.tick());
            startHuntingPhase(huntingPhaseIndex + 1);
            ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
        }
    }

    void unlockGhosts() {
        Ghost blinky = ghost(RED_GHOST);
        if (blinky.inState(LOCKED)) {
            if (blinky.insideHouse(world.house())) {
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

    @Override
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

    final List<GameEventListener> gameEventListeners = new ArrayList<>();

    @Override
    public void addGameEventListener(GameEventListener listener) {
        checkNotNull(listener);
        gameEventListeners.add(listener);
    }

    @Override
    public void publishGameEvent(GameEventType type) {
        publishGameEvent(new GameEvent(type, this));
    }

    @Override
    public void publishGameEvent(GameEventType type, Vector2i tile) {
        publishGameEvent(new GameEvent(type, this, tile));
    }

    @Override
    public void publishGameEvent(GameEvent event) {
        Logger.trace("Game event: {}", event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}
