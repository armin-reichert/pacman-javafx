/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Common base class of all game models.
 *
 * @author Armin Reichert
 */
public abstract class GameModel {

    // Common Pac-Man/Ms. Pac-Man animation IDs
    public static final String ANIM_PAC_MUNCHING     = "munching";
    public static final String ANIM_PAC_DYING        = "dying";

    // Common ghost animation IDs
    public static final String ANIM_GHOST_NORMAL     = "normal";
    public static final String ANIM_GHOST_FRIGHTENED = "frightened";
    public static final String ANIM_GHOST_EYES       = "eyes";
    public static final String ANIM_GHOST_FLASHING   = "flashing";
    public static final String ANIM_GHOST_NUMBER     = "number";

    // Ghost IDs
    public static final byte RED_GHOST = 0, PINK_GHOST = 1, CYAN_GHOST = 2, ORANGE_GHOST = 3;

    /** Game loop frequency, ticks per second. */
    public static final byte       TICKS_PER_SECOND = 60;

    public static final short      POINTS_ALL_GHOSTS_IN_LEVEL = 12_000;
    public static final byte       LEVEL_COUNTER_MAX_SIZE = 7;
    public static final byte       BONUS_POINTS_SHOWN_TICKS = 120; // unsure
    public static final short[]    KILLED_GHOST_VALUES = { 200, 400, 800, 1600 };

    protected final File           userDir;
    protected final List<Byte>     levelCounter = new ArrayList<>();
    protected final GateKeeper     gateKeeper = new GateKeeper();
    protected final ScoreManager   scoreManager = new ScoreManager(this);
    protected HuntingControl       huntingControl;
    protected File                 customMapDir;
    protected boolean              levelCounterEnabled;
    protected boolean              playing;
    protected int                  initialLives;
    protected int                  lives;
    protected SimulationStepLog    eventLog;

    protected GameLevel            level;

    public abstract boolean      canStartNewGame();
    public abstract void         endGame();
    public abstract void         onPacKilled();
    public abstract void         activateNextBonus();

    public abstract int          intermissionNumberAfterLevel();
    public abstract int          numFlashes();

    public abstract float        ghostAttackSpeed(Ghost ghost);
    public abstract float        ghostFrightenedSpeed(Ghost ghost);
    public abstract float        ghostSpeedInsideHouse(Ghost ghost);
    public abstract float        ghostSpeedReturningToHouse(Ghost ghost);
    public abstract float        ghostTunnelSpeed(Ghost ghost);

    public abstract float        pacNormalSpeed();
    public abstract long         pacPowerTicks();
    public abstract long         pacPowerFadingTicks();
    public abstract float        pacPowerSpeed();
    public abstract long         gameOverStateTicks();
    public abstract void         setDemoLevelBehavior();

    protected abstract void      configureNormalLevel();
    protected abstract void      configureDemoLevel();
    protected abstract boolean   isPacManKillingIgnored();
    protected abstract void      setActorBaseSpeed(int levelNumber);
    protected abstract void      initScore(int levelNumber);
    protected abstract boolean   isBonusReached();
    protected abstract byte      computeBonusSymbol();

    protected abstract void      onPelletOrEnergizerEaten(Vector2i tile, int remainingFoodCount, boolean energizer);

    protected GameModel(File userDir) {
        this.userDir = userDir;
        this.customMapDir = new File(userDir, "maps");
    }

    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    public File customMapDir() {
        return customMapDir;
    }

    public void updateCustomMaps() {}

    public HuntingControl huntingControl() {
        return huntingControl;
    }

    public void reset() {
        playing = false;
        lives = initialLives;
        level = null;
        scoreManager.resetScore();
    }

    public void startNewGame() {
        reset();
        createLevel(1);
        publishGameEvent(GameEventType.GAME_STARTED);
    }

    public void deleteLevel() {
        level = null;
    }

    public void createLevel(int levelNumber) {
        level = new GameLevel(levelNumber);
        configureNormalLevel();
        scoreManager.setLevelNumber(levelNumber);
        scoreManager.setScoreEnabled(true);
        huntingControl.reset();
        updateLevelCounter();
        Logger.info("Level {} created", levelNumber);
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    public void startNextLevel() {
        createLevel(level.number + 1);
        startLevel();
        showGuys();
    }

    public void createDemoLevel() {
        level = new GameLevel(1);
        level.setDemoLevel(true);
        configureDemoLevel();
        Logger.info("Demo Level created");
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    protected void updateLevelCounter() {
        if (level.number == 1) {
            levelCounter.clear();
        }
        if (levelCounterEnabled) {
            levelCounter.add(level.bonusSymbol(0));
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SIZE) {
                levelCounter.removeFirst();
            }
        }
    }

    public void startLevel() {
        gateKeeper.setLevelNumber(level.number);
        setActorBaseSpeed(level.number);
        initScore(level.number);
        letsGetReadyToRumble();
        level.setStartTime(System.currentTimeMillis());
        Logger.info("{} started", level.isDemoLevel() ? "Demo Level" : "Level " + level.number);
        Logger.info("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
        level.ghosts().forEach(ghost -> Logger.info("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    /**
     * Sets each guy to his start position and resets him to his initial state. The guys are all initially invisible!
     */
    public void letsGetReadyToRumble() {
        level.pac().reset(); // invisible!
        level.pac().setPosition(level.world().pacPosition());
        level.pac().setMoveAndWishDir(Direction.LEFT);
        level.ghosts().forEach(ghost -> {
            ghost.reset(); // invisible!
            ghost.setPosition(level.world().ghostPosition(ghost.id()));
            ghost.setMoveAndWishDir(level.world().ghostDirection(ghost.id()));
            ghost.setState(LOCKED);
        });
        initActorAnimations();
        level.powerTimer().resetIndefinitely();
        level.blinking().setStartPhase(Pulse.ON); // Energizers are visible when ON
        level.blinking().reset();
    }

    protected void initActorAnimations() {
        level.pac().selectAnimation(GameModel.ANIM_PAC_MUNCHING);
        level.pac().animations().ifPresent(Animations::resetCurrentAnimation);
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void showGuys() {
        level.pac().setVisible(true);
        level.ghosts().forEach(ghost -> ghost.setVisible(true));
    }

    public void hideGuys() {
        level.pac().setVisible(false);
        level.ghosts().forEach(ghost -> ghost.setVisible(false));
    }

    public SimulationStepLog eventLog() {
        return eventLog;
    }

    public void clearEventLog() {
        eventLog = new SimulationStepLog();
    }

    protected Vector2i scatterTarget(Ghost ghost) {
        return level.world().ghostScatterTile(ghost.id());
    }

    protected Vector2i chasingTarget(Ghost ghost) {
        return switch (ghost.id()) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> level.pac().tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> level.pac().tilesAhead(4, hasOverflowBug());
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> level.pac().tilesAhead(2, hasOverflowBug()).scaled(2).minus(level.ghost(RED_GHOST).tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghost.tile().euclideanDistance(level.pac().tile()) < 8 ? scatterTarget(ghost) : level.pac().tile();
            default -> throw GameException.illegalGhostID(ghost.id());
        };
    }

    /**
     * See this <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">explanation</a>.
     */
    protected boolean hasOverflowBug() {
        return true;
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

    public ScoreManager scoreManager() {
        return scoreManager;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void onLevelCompleted() {
        level.blinking().setStartPhase(Pulse.OFF);
        level.blinking().reset();
        level.pac().freeze();
        level.bonus().ifPresent(Bonus::setInactive);
        // when cheating, there might still be food
        level.world().map().food().tiles().forEach(level.world()::registerFoodEatenAt);
        huntingControl.stop();
        Logger.info("Hunting timer stopped");
        level.powerTimer().stop();
        level.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero");
        Logger.trace("Game level {} completed.", level.number);
    }

    public boolean isLevelComplete() {
        return level.world().uneatenFoodCount() == 0;
    }

    public boolean isPacManKilled() {
        return eventLog.pacKilled;
    }

    public boolean areGhostsKilled() {
        return !eventLog.killedGhosts.isEmpty();
    }

    public void doHuntingStep() {
        if (huntingControl.isCurrentPhaseOver()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", huntingControl.phaseIndex(), huntingControl.phaseType(), huntingControl.currentTick());
            huntingControl.startNextPhase(level.number);
        } else {
            huntingControl.update();
        }

        level.blinking().tick();
        gateKeeper.unlockGhosts(level, this::onGhostReleased, eventLog);

        checkForFood(level.pac().tile());
        level.pac().update(this);
        updatePacPower();
        checkPacKilled();
        if (eventLog.pacKilled) {
            return;
        }

        level.ghosts().forEach(ghost -> ghost.update(this));
        level.ghosts(FRIGHTENED).filter(level.pac()::sameTile).forEach(this::killGhost);
        if (!eventLog.killedGhosts.isEmpty()) {
            return;
        }
        level.bonus().ifPresent(this::updateBonus);
    }

    private void checkPacKilled() {
        boolean pacMeetsKiller = level.ghosts(HUNTING_PAC).anyMatch(level.pac()::sameTile);
        if (level.isDemoLevel()) {
            eventLog.pacKilled = pacMeetsKiller && !isPacManKillingIgnored();
        } else {
            eventLog.pacKilled = pacMeetsKiller && !level.pac().isImmune();
        }
    }

    private void checkForFood(Vector2i tile) {
        if (!level.world().hasFoodAt(tile)) {
            level.pac().starve();
            return;
        }
        level.pac().endStarving();
        eventLog.foodFoundTile = tile;
        eventLog.energizerFound = level.world().isEnergizerPosition(tile);
        level.world().registerFoodEatenAt(tile);
        // let specific game do its stuff:
        onPelletOrEnergizerEaten(tile, level.world().uneatenFoodCount(), eventLog.energizerFound);
        publishGameEvent(GameEventType.PAC_FOUND_FOOD, tile);
    }

    protected void processEatenEnergizer() {
        level.victims().clear(); // ghosts eaten using this energizer
        if (pacPowerTicks() > 0) {
            eventLog.pacGetsPower = true;
            huntingControl.stop();
            level.powerTimer().restartTicks(pacPowerTicks());
            Logger.info("Hunting paused, power timer restarted, duration={} ticks", level.powerTimer().duration());
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            level.ghosts(FRIGHTENED).forEach(Ghost::reverseASAP);
            publishGameEvent(GameEventType.PAC_GETS_POWER);
        } else {
            level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseASAP);
        }
    }

    private void updatePacPower() {
        level.powerTimer().tick();
        if (level.powerTimer().remaining() == pacPowerFadingTicks()) {
            eventLog.pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (level.powerTimer().hasExpired()) {
            level.powerTimer().stop();
            level.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingControl.start();
            Logger.info("Hunting timer started");
            level.ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean isPowerFading() {
        return level.powerTimer().isRunning() && level.powerTimer().remaining() <= pacPowerFadingTicks();
    }

    public boolean isPowerFadingStarting() {
        return level.powerTimer().isRunning() && level.powerTimer().remaining() == pacPowerFadingTicks()
            || level.powerTimer().duration() < pacPowerFadingTicks() && level.powerTimer().currentTick() == 1;
    }

    private void updateBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE && level.pac().sameTile(bonus.entity())) {
            bonus.setEaten(BONUS_POINTS_SHOWN_TICKS);
            scoreManager.scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog.bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }

    protected abstract void onGhostReleased(Ghost ghost);

    public void killGhost(Ghost ghost) {
        eventLog.killedGhosts.add(ghost);
        int killedSoFar = level.victims().size();
        int points = KILLED_GHOST_VALUES[killedSoFar];
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        level.addKilledGhost();
        if (level.killedGhostCount() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_IN_LEVEL;
            scoreManager.scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number);
        }
        level.victims().add(ghost);
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
}