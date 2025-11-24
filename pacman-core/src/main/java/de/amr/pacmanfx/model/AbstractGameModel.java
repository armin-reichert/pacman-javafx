/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Common base class of all Pac-Man game models.
 */
public abstract class AbstractGameModel implements Game {

    private static final CollisionStrategy DEFAULT_COLLISION_STRATEGY = CollisionStrategy.SAME_TILE;

    private static final float COLLISION_SENSITIVITY_PIXELS = 2;

    protected ObjectProperty<CollisionStrategy> collisionStrategy;

    protected final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    protected final ObjectProperty<GameLevel> gameLevel = new SimpleObjectProperty<>();

    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    protected final IntegerProperty lifeCount = new SimpleIntegerProperty(0);

    protected final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final SimulationStepResult thisStep = new SimulationStepResult();

    protected final ScoreManager scoreManager = new ScoreManager(this);

    protected GameStateMachine stateMachine;

    protected abstract void checkPacFindsFood(GameLevel gameLevel);

    protected abstract void checkPacFindsBonus(GameLevel gameLevel);

    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    protected abstract void resetPacManAndGhostAnimations(GameLevel gameLevel);

    public final ObjectProperty<CollisionStrategy> collisionStrategyProperty() {
        if (collisionStrategy == null) {
            collisionStrategy = new SimpleObjectProperty<>(DEFAULT_COLLISION_STRATEGY);
        }
        return collisionStrategy;
    }

    public CollisionStrategy collisionStrategy() {
        return collisionStrategy != null ? collisionStrategy.get() : DEFAULT_COLLISION_STRATEGY;
    }

    public void setCollisionStrategy(CollisionStrategy collisionStrategy) {
        collisionStrategyProperty().set(collisionStrategy);
    }

    @Override
    public final GameStateMachine stateMachine() {
        return stateMachine;
    }

    public void setStateMachine(GameStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * @param either some actor
     * @param other some actor
     * @return <code>true</code> if both entities collide according to the current collision strategy
     */
    public boolean actorsCollide(Actor either, Actor other) {
        requireNonNull(either, "Actor to check for collision must not be null");
        requireNonNull(other, "Actor to check for collision must not be null");
        return switch (collisionStrategy()) {
            case SAME_TILE -> either.tile().equals(other.tile());
            case CENTER_DISTANCE -> {
                float dist = either.center().euclideanDist(other.center());
                if (dist < COLLISION_SENSITIVITY_PIXELS) {
                    Logger.info("Collision detected (dist={}): {} collides with {}", dist, either, other);
                    yield true;
                }
                yield false;
            }
        };
    }

    public void setLifeCount(int n) {
        if (n >= 0) {
            lifeCount.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel.set(gameLevel);
    }

    public GameLevel gameLevel() {
        return gameLevel.get();
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(gameLevel.get());
    }

    @Override
    public SimulationStepResult simulationStepResult() {
        return thisStep;
    }

    @Override
    public ScoreManager scoreManager() {
        return scoreManager;
    }

    @Override
    public int lifeCount() {
        return lifeCount.get();
    }

    @Override
    public void addLives(int n) {
        setLifeCount(lifeCount() + n);
    }

    @Override
    public boolean isPlaying() { return playing.get(); }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public void continueGame(GameLevel gameLevel) {
        resetPacManAndGhostAnimations(gameLevel);
        gameLevel.getReadyToPlay();
        gameLevel.showPacAndGhosts();
        publishEvent(GameEventType.GAME_CONTINUED);
    }

    @Override
    public boolean hasPacManBeenKilled() {
        return thisStep.pacKiller != null;
    }

    @Override
    public boolean hasGhostBeenKilled() {
        return !thisStep.killedGhosts.isEmpty();
    }

    @Override
    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    @Override
    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }

    @Override
    public int initialLifeCount() {
        return initialLifeCount.get();
    }

    @Override
    public void setInitialLifeCount(int count) {
        initialLifeCount.set(count);
    }

    @Override
    public boolean isLevelCompleted(GameLevel gameLevel) {
        return gameLevel.worldMap().foodLayer().uneatenFoodCount() == 0;
    }

    @Override
    public void startHunting(GameLevel gameLevel) {
        gameLevel.pac().optAnimationManager().ifPresent(AnimationManager::play);
        gameLevel.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        gameLevel.blinking().setStartState(Pulse.State.ON);
        gameLevel.blinking().restart();
        gameLevel.huntingTimer().startFirstPhase();
        publishEvent(GameEventType.HUNTING_PHASE_STARTED);
    }

    protected void makeHuntingStep(GameLevel gameLevel) {
        final TerrainLayer terrain = gameLevel.worldMap().terrainLayer();
        // Ghosts colliding with Pac?
        thisStep.ghostsCollidingWithPac.clear();
        gameLevel.ghosts()
            .filter(ghost -> !terrain.isTileInPortalSpace(ghost.tile()))
            .filter(ghost -> actorsCollide(ghost, gameLevel.pac()))
            .forEach(thisStep.ghostsCollidingWithPac::add);

        if (!thisStep.ghostsCollidingWithPac.isEmpty()) {
            // Pac killed? Might stay alive when immune or in demo level safe time!
            checkPacKilled(gameLevel);
            if (hasPacManBeenKilled()) return;

            // Ghost(s) killed?
            thisStep.ghostsCollidingWithPac.stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(thisStep.killedGhosts::add);
            if (hasGhostBeenKilled()) {
                thisStep.killedGhosts.forEach(ghost -> onGhostKilled(gameLevel, ghost));
                return;
            }
        }

        checkPacFindsFood(gameLevel);
        checkPacFindsBonus(gameLevel);

        updatePacPower(gameLevel);
        gameLevel.blinking().tick();
        gameLevel.huntingTimer().update();
    }

    @Override
    public void onLevelCompleted(GameLevel gameLevel) {
        gameLevel.huntingTimer().stop();
        // If level was ended by cheat, there might still be food remaining, so eat it:
        gameLevel.worldMap().foodLayer().eatAll();
        gameLevel.blinking().setStartState(Pulse.State.OFF);
        gameLevel.blinking().reset();
        gameLevel.ghosts().forEach(Ghost::stopAnimation);
        gameLevel.pac().onLevelCompleted();
        gameLevel.bonus().ifPresent(Bonus::setInactive);
    }

    protected void checkPacKilled(GameLevel gameLevel) {
        boolean demoLevel = gameLevel.isDemoLevel();
        if (demoLevel && isPacSafeInDemoLevel(gameLevel) || !demoLevel && gameLevel.pac().isImmune()) {
            return;
        }
        thisStep.pacKiller = thisStep.ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().orElse(null);
    }

    protected void updatePacPower(GameLevel gameLevel) {
        final Pac pac = gameLevel.pac();
        final TickTimer powerTimer = pac.powerTimer();
        powerTimer.doTick();
        if (pac.isPowerFadingStarting(gameLevel)) {
            thisStep.pacStartsLosingPower = true;
            Logger.info("{} starts losing power", pac.name());
            publishEvent(GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (powerTimer.hasExpired()) {
            thisStep.pacLostPower = true;
            Logger.info("{} lost power", pac.name());
            powerTimer.stop();
            powerTimer.reset(0);
            Logger.info("Power timer stopped and reset to zero");
            gameLevel.energizerVictims().clear();
            gameLevel.huntingTimer().start();
            Logger.info("Hunting timer restarted because {} lost power", pac.name());
            gameLevel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
            publishEvent(GameEventType.PAC_LOST_POWER);
        }
    }

    // GameEventManager implementation

    private final List<GameEventListener> eventListeners = new ArrayList<>();

    @Override
    public void addEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void removeEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void publishEvent(GameEvent event) {
        requireNonNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    @Override
    public void publishEvent(GameEventType type) {
        requireNonNull(type);
        publishEvent(new GameEvent(this, type));
    }

    @Override
    public void publishEvent(GameEventType type, Vector2i tile) {
        requireNonNull(type);
        publishEvent(new GameEvent(this, type, tile));
    }
}