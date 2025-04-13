/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * Common base class of all Pac-Man game models.
 *
 * @author Armin Reichert
 */
public abstract class GameModel {

    // Ghost IDs
    public static final byte RED_GHOST_ID = 0, PINK_GHOST_ID = 1, CYAN_GHOST_ID = 2, ORANGE_GHOST_ID = 3;

    public static final byte LEVEL_COUNTER_MAX_SIZE = 7;
    public static final short POINTS_ALL_GHOSTS_EATEN_IN_LEVEL = 12_000;
    public static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = {2, 4, 8, 16}; // factor * 100 = value

    protected final List<Byte> levelCounter = new ArrayList<>();
    protected final GateKeeper gateKeeper = new GateKeeper();
    protected final ScoreManager scoreManager = new ScoreManager();
    protected final HuntingTimer huntingTimer;

    protected GameLevel level;
    protected long levelStartTime;
    protected int lastLevelNumber;

    private final BooleanProperty cutScenesEnabledPy = new SimpleBooleanProperty(true);
    private final BooleanProperty demoLevelPy = new SimpleBooleanProperty(false);
    private final IntegerProperty initialLivesPy = new SimpleIntegerProperty(0);
    private final BooleanProperty levelCounterEnabledPy = new SimpleBooleanProperty();
    private final IntegerProperty livesPy = new SimpleIntegerProperty(0);
    private final BooleanProperty playingPy = new SimpleBooleanProperty(false);
    private final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(false);
    private final BooleanProperty simulateOverflowBugPy = new SimpleBooleanProperty(true);

    protected SimulationStepLog eventLog;

    protected GameModel(HuntingTimer huntingTimer) {
        this.huntingTimer = assertNotNull(huntingTimer);
        scoreManager.setOnExtraLifeWon(extraLifeScore -> {
            eventLog.extraLifeWon = true;
            eventLog.extraLifeScore = extraLifeScore;
            addLives(1);
            publishGameEvent(GameEventType.EXTRA_LIFE_WON);
        });
    }

    public abstract void init();

    public abstract void resetEverything();

    public abstract void resetForStartingNewGame();

    public abstract boolean canStartNewGame();

    public abstract boolean continueOnGameOver();

    public abstract boolean isOver();

    public abstract void endGame();

    public abstract void onPacKilled();

    public abstract void killGhost(Ghost ghost);

    public abstract void activateNextBonus();

    protected abstract void setActorBaseSpeed(int levelNumber);

    public abstract float ghostAttackSpeed(Ghost ghost);

    public abstract float ghostFrightenedSpeed(Ghost ghost);

    public abstract float ghostSpeedInsideHouse(Ghost ghost);

    public abstract float ghostSpeedReturningToHouse(Ghost ghost);

    public abstract float ghostTunnelSpeed(Ghost ghost);

    public abstract float pacNormalSpeed();

    public abstract long pacPowerTicks();

    public abstract long pacPowerFadingTicks();

    public abstract float pacPowerSpeed();

    public abstract long gameOverStateTicks();

    protected abstract void buildNormalLevel(int levelNumber);

    protected abstract void buildDemoLevel();

    public abstract void assignDemoLevelBehavior(Pac pac);

    protected abstract boolean isPacManKillingIgnored();

    protected abstract boolean isBonusReached();

    protected abstract byte computeBonusSymbol(int levelNumber);

    protected abstract void onFoodEaten(Vector2i tile, int remainingFoodCount, boolean energizer);

    protected abstract void onGhostReleased(Ghost ghost);

    public abstract MapSelector mapSelector();

    public final HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    public Optional<GameLevel> level() {
        return Optional.ofNullable(level);
    }

    public final int lastLevelNumber() {
        return lastLevelNumber;
    }

    public List<Byte> levelCounter() {
        return levelCounter;
    }

    public ScoreManager scoreManager() {
        return scoreManager;
    }

    public BooleanProperty cutScenesEnabledProperty() { return cutScenesEnabledPy; }
    public BooleanProperty demoLevelProperty() { return demoLevelPy; }
    public IntegerProperty initialLivesProperty() { return initialLivesPy; }
    public BooleanProperty levelCounterEnabledProperty() { return levelCounterEnabledPy; }
    public IntegerProperty livesProperty() { return livesPy; }
    public BooleanProperty playingProperty() { return playingPy; }
    public BooleanProperty scoreVisibleProperty() { return scoreVisiblePy; }
    public BooleanProperty simulateOverflowBugProperty() { return simulateOverflowBugPy; }

    public void loseLife() {
        int lives = livesProperty().get();
        if (lives > 0) {
            livesProperty().set(lives - 1);
        } else {
            Logger.error("Cannot lose life, no lives left");
        }
    }

    public void addLives(int lives) {
        livesProperty().set(livesProperty().get() + lives);
    }

    public void startNewGame() {
        resetForStartingNewGame();
        createNormalLevel(1);
        publishGameEvent(GameEventType.GAME_STARTED);
    }

    public void createNormalLevel(int levelNumber) {
        demoLevelProperty().set(false);
        buildNormalLevel(levelNumber);
        scoreManager.setLevelNumber(levelNumber);
        huntingTimer.reset();
        updateLevelCounter();
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    public void createDemoLevel() {
        demoLevelProperty().set(true);
        buildDemoLevel();
        publishGameEvent(GameEventType.LEVEL_CREATED);
    }

    protected void updateLevelCounter() {
        if (level.number() == 1) {
            levelCounter.clear();
        }
        if (levelCounterEnabledProperty().get()) {
            levelCounter.add(level.bonusSymbol(0));
            if (levelCounter.size() > LEVEL_COUNTER_MAX_SIZE) {
                levelCounter.removeFirst();
            }
        }
    }

    public void startLevel() {
        gateKeeper.setLevelNumber(level.number());
        scoreManager.setLevelNumber(level.number());
        scoreManager.setScoreEnabled(!demoLevelProperty().get());
        scoreManager.setHighScoreEnabled(!demoLevelProperty().get());
        letsGetReadyToRumble();
        setActorBaseSpeed(level.number());
        Logger.debug("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
        level.ghosts().forEach(ghost -> Logger.debug("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));
        level.showMessage(GameLevel.Message.READY);
        levelStartTime = System.currentTimeMillis();
        Logger.info("{} started", demoLevelProperty().get() ? "Demo Level" : "Level " + level.number());
        publishGameEvent(GameEventType.LEVEL_STARTED);
    }

    public void startNextLevel() {
        int nextLevelNumber = level.number() + 1;
        if (nextLevelNumber <= lastLevelNumber) {
            createNormalLevel(nextLevelNumber);
            startLevel();
            showGuys();
        } else {
            Logger.warn("Last level ({}) reached, cannot start next level", lastLevelNumber);
        }
    }

    /**
     * Sets each guy to his start position and resets him to his initial state. The guys are all initially invisible!
     */
    public void letsGetReadyToRumble() {
        level.pac().reset(); // invisible!
        level.pac().setPosition(level.pacPosition());
        level.pac().setMoveAndWishDir(Direction.LEFT);
        level.ghosts().forEach(ghost -> {
            ghost.reset(); // invisible!
            ghost.setPosition(level.ghostPosition(ghost.id()));
            ghost.setMoveAndWishDir(level.ghostDirection(ghost.id()));
            ghost.setState(LOCKED);
        });
        initActorAnimations();
        level.powerTimer().resetIndefiniteTime();
        level.blinking().setStartPhase(Pulse.ON); // Energizers are visible when ON
        level.blinking().reset();
    }

    protected void initActorAnimations() {
        level.pac().selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }

    public void showGuys() {
        level.pac().show();
        level.ghosts().forEach(Actor2D::show);
    }

    public void hideGuys() {
        level.pac().hide();
        level.ghosts().forEach(Actor2D::hide);
    }

    public SimulationStepLog eventLog() {
        return eventLog;
    }

    public void clearEventLog() {
        eventLog = new SimulationStepLog();
    }

    /**
     * Returns the chasing target tile for the given chaser.
     *
     * @param ghostID the chasing ghost's ID
     * @param level the game level
     * @param buggy if overflow bug from Arcade game is simulated
     * @see <a href="http://www.donhodges.com/pacman_pinky_explanation.htm">Overflow bug explanation</a>.
     */
    protected Vector2i chasingTargetTile(byte ghostID, GameLevel level, boolean buggy) {
        return switch (ghostID) {
            // Blinky (red ghost) attacks Pac-Man directly
            case RED_GHOST_ID -> level.pac().tile();

            // Pinky (pink ghost) ambushes Pac-Man
            case PINK_GHOST_ID -> level.pac().tilesAhead(4, buggy);

            // Inky (cyan ghost) attacks from opposite side as Blinky
            case CYAN_GHOST_ID -> level.pac().tilesAhead(2, buggy).scaled(2).minus(level.ghost(RED_GHOST_ID).tile());

            // Clyde/Sue (orange ghost) attacks directly or retreats towards scatter target if Pac is near
            case ORANGE_GHOST_ID -> level.ghost(ORANGE_GHOST_ID).tile().euclideanDist(level.pac().tile()) < 8
                ? level.ghostScatterTile(ORANGE_GHOST_ID) : level.pac().tile();

            default -> throw GameException.invalidGhostID(ghostID);
        };
    }

    public void onLevelCompleted() {
        level.blinking().setStartPhase(Pulse.OFF);
        level.blinking().reset();
        level.pac().freeze();
        level.bonus().ifPresent(Bonus::setInactive);
        // when cheating, there might still be food
        level.worldMap().tiles()
            .filter(level::hasFoodAt)
            .forEach(level::registerFoodEatenAt);
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
        level.powerTimer().stop();
        level.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero");
        Logger.trace("Game level {} completed.", level.number());
    }

    public boolean isLevelComplete() {
        return level.uneatenFoodCount() == 0;
    }

    public boolean isPacManKilled() {
        return eventLog.pacKilled;
    }

    public boolean areGhostsKilled() {
        return !eventLog.killedGhosts.isEmpty();
    }

    public void startHunting() {
        level.pac().startAnimation();
        level.ghosts().forEach(Ghost::startAnimation);
        level.blinking().setStartPhase(Pulse.ON);
        level.blinking().restart(Integer.MAX_VALUE);
        huntingTimer.startFirstHuntingPhase(level.number());
        publishGameEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    public void doHuntingStep() {
        huntingTimer.update(level.number());
        level.blinking().tick();
        gateKeeper.unlockGhosts(level, this::onGhostReleased, eventLog);
        checkForFood();
        level.pac().update(this);
        updatePacPower();
        checkPacKilled();
        if (!eventLog.pacKilled) {
            level.ghosts().forEach(ghost -> ghost.update(this));
            level.ghosts(FRIGHTENED).filter(ghost -> areColliding(ghost, level.pac())).forEach(this::killGhost);
            if (eventLog.killedGhosts.isEmpty()) {
                level.bonus().ifPresent(this::updateBonus);
            }
        }
    }

    private void checkPacKilled() {
        boolean pacMeetsKiller = level.ghosts(HUNTING_PAC).anyMatch(ghost -> areColliding(level.pac(), ghost));
        if (demoLevelProperty().get()) {
            eventLog.pacKilled = pacMeetsKiller && !isPacManKillingIgnored();
        } else {
            eventLog.pacKilled = pacMeetsKiller && !level.pac().isImmune();
        }
    }

    private void checkForFood() {
        Vector2i tile = level.pac().tile();
        if (level.hasFoodAt(tile)) {
            eventLog.foodFoundTile = tile;
            eventLog.energizerFound = level.isEnergizerPosition(tile);
            level.registerFoodEatenAt(tile);
            onFoodEaten(tile, level.uneatenFoodCount(), eventLog.energizerFound);
            level.pac().endStarving();
            publishGameEvent(GameEventType.PAC_FOUND_FOOD, tile);
        } else {
            level.pac().starve();
        }
    }

    protected boolean areColliding(Actor2D actor, Actor2D otherActor) {
        return actor.sameTile(otherActor);
    }

    protected void onEnergizerEaten() {
        level.victims().clear(); // ghosts eaten using this energizer
        long powerTicks = pacPowerTicks();
        if (powerTicks > 0) {
            huntingTimer.stop();
            Logger.info("Hunting stopped");
            level.powerTimer().restartTicks(powerTicks);
            Logger.info("Power timer restarted, duration={} ticks ({0.00} sec)", powerTicks, powerTicks / 60.0);
            level.ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
            level.ghosts(FRIGHTENED).forEach(Ghost::reverseASAP);
            eventLog.pacGetsPower = true;
            publishGameEvent(GameEventType.PAC_GETS_POWER);
        } else {
            level.ghosts(FRIGHTENED, HUNTING_PAC).forEach(Ghost::reverseASAP);
        }
    }

    private void updatePacPower() {
        level.powerTimer().doTick();
        if (isPowerFadingStarting(level.powerTimer())) {
            eventLog.pacStartsLosingPower = true;
            publishGameEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (level.powerTimer().hasExpired()) {
            level.powerTimer().stop();
            level.powerTimer().reset(0);
            Logger.info("Power timer stopped and reset to zero");
            level.victims().clear();
            huntingTimer.start();
            Logger.info("Hunting timer restarted");
            level.ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
            eventLog.pacLostPower = true;
            publishGameEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    public boolean isPowerFading(TickTimer powerTimer) {
        return powerTimer.isRunning() && powerTimer.remainingTicks() <= pacPowerFadingTicks();
    }

    public boolean isPowerFadingStarting(TickTimer powerTimer) {
        return powerTimer.isRunning() && powerTimer.remainingTicks() == pacPowerFadingTicks()
            || powerTimer.durationTicks() < pacPowerFadingTicks() && powerTimer.tickCount() == 1;
    }

    private void updateBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE && areColliding(level.pac(), bonus.actor())) {
            bonus.setEaten(120); //TODO is 2 seconds correct?
            scoreManager.scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
            eventLog.bonusEaten = true;
            publishGameEvent(GameEventType.BONUS_EATEN);
        } else {
            bonus.update(this);
        }
    }

    // Game Event Support

    private final List<GameEventListener> gameEventListeners = new ArrayList<>();

    public void addGameEventListener(GameEventListener listener) {
        assertNotNull(listener);
        if (!gameEventListeners.contains(listener)) {
            gameEventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    public void removeGameEventListener(GameEventListener listener) {
        assertNotNull(listener);
        boolean removed = gameEventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    public void publishGameEvent(GameEvent event) {
        assertNotNull(event);
        gameEventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    public void publishGameEvent(GameEventType type) {
        assertNotNull(type);
        publishGameEvent(new GameEvent(type, this));
    }

    public void publishGameEvent(GameEventType type, Vector2i tile) {
        assertNotNull(type);
        publishGameEvent(new GameEvent(type, this, tile));
    }
}