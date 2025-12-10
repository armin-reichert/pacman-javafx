/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static java.util.Objects.requireNonNull;

/**
 * Common base class of all Pac-Man game models.
 */
public abstract class AbstractGameModel implements Game {

    public static final CollisionStrategy DEFAULT_COLLISION_STRATEGY = CollisionStrategy.SAME_TILE;

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(DEFAULT_COLLISION_STRATEGY);

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();

    private final BooleanProperty immune = new SimpleBooleanProperty(false);

    private final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    private final IntegerProperty lifeCount = new SimpleIntegerProperty(0);

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);


    protected final GameControl gameControl;

    protected final SimulationStepResult simulationStepResult = new SimulationStepResult();

    protected AbstractGameModel(GameControl gameControl) {
        this.gameControl = requireNonNull(gameControl);
        gameControl.stateMachine().setContext(this);
        gameControl.stateMachine().addStateChangeListener(
            (oldState, newState) -> publishGameEvent(new GameStateChangeEvent(this, oldState, newState)));

        cheatUsedProperty().addListener((py, ov, cheated) -> {
            if (cheated && highScore.isEnabled()) {
                highScore.setEnabled(false);
            }
        });

        lifeCountProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() < 0) {
                throw new IllegalArgumentException("Life count cannot be set to negative value " + nv.intValue());
            }
        });

        score.pointsProperty().addListener((py, oldScore, newScore) -> {
            // has extra life score line been crossed?
            for (int scoreLine : extraLifeScores) {
                if (oldScore.intValue() < scoreLine && newScore.intValue() >= scoreLine) {
                    simulationStepResult.extraLifeWon = true;
                    simulationStepResult.extraLifeScore = scoreLine;
                    break;
                }
            }
            if (simulationStepResult.extraLifeWon) {
                addLives(1);
                publishGameEvent(new GameEvent(this, GameEvent.Type.SPECIAL_SCORE_REACHED));
            }
        });
    }

    public ObjectProperty<GameLevel> levelProperty() {
        return level;
    }

    // To be implemented by subclasses

    protected abstract void checkPacFindsFood(GameLevel level);

    protected abstract void checkPacFindsBonus(GameLevel level);

    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    // These methods are public such that info panel can call them

    public abstract float ghostSpeedAttacking(GameLevel level, Ghost ghost, int cruiseElroy);

    public abstract float ghostSpeedWhenFrightened(GameLevel level);

    public abstract float ghostSpeedTunnel(int levelNumber);

    // Game interface

    @Override
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    @Override
    public BooleanProperty immuneProperty() {
        return immune;
    }

    @Override
    public BooleanProperty usingAutopilotProperty() {
        return usingAutopilot;
    }

    @Override
    public final GameControl control() {
        return gameControl;
    }

    @Override
    public SimulationStepResult simulationStepResult() {
        return simulationStepResult;
    }

    @Override
    public GameLevel level() {
        return level.get();
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(level.get());
    }

    @Override
    public IntegerProperty lifeCountProperty() {
        return lifeCount;
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public Score highScore() {
        return highScore;
    }

    @Override
    public int lifeCount() {
        return lifeCountProperty().get();
    }

    @Override
    public void addLives(int n) {
        lifeCountProperty().set(lifeCount() + n);
    }

    @Override
    public CollisionStrategy collisionStrategy() {
        return collisionStrategy.get();
    }

    @Override
    public void setCollisionStrategy(CollisionStrategy strategy) {
        requireNonNull(strategy);
        collisionStrategy.set(strategy);
    }

    @Override
    public boolean isPlaying() { return playing.get(); }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public boolean hasPacManBeenKilled() {
        return simulationStepResult.pacKiller != null;
    }

    @Override
    public boolean hasGhostBeenKilled() {
        return !simulationStepResult.killedGhosts.isEmpty();
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
    public boolean isLevelCompleted() {
        return level().worldMap().foodLayer().uneatenFoodCount() == 0;
    }

    @Override
    public void startHunting(GameLevel level) {
        level.huntingTimer().startFirstPhase(level.number());

        //TODO move into UI layer?
        level.blinking().setStartState(Pulse.State.ON);
        level.blinking().restart();
        level.pac().optAnimationManager().ifPresent(AnimationManager::play);
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));

        publishGameEvent(GameEvent.Type.HUNTING_PHASE_STARTED);
    }

    @Override
    public void onLevelCompleted() {
        final GameLevel level = level();
        final Pac pac = level.pac();

        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");

        level.optBonus().ifPresent(Bonus::setInactive);

        pac.setSpeed(0);
        //TODO move animation-related stuff into UI layer?
        pac.stopAnimation();
        pac.selectAnimation(CommonAnimationID.ANIM_PAC_FULL);
        level.blinking().setStartState(Pulse.State.OFF);
        level.blinking().reset();
        level.ghosts().forEach(Ghost::stopAnimation);
    }

    // GameEventManager implementation

    private final Set<GameEventListener> eventListeners = new HashSet<>();

    @Override
    public void addGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void removeGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void publishGameEvent(GameEvent event) {
        requireNonNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    @Override
    public void publishGameEvent(GameEvent.Type type) {
        requireNonNull(type);
        publishGameEvent(new GameEvent(this, type));
    }

    @Override
    public void publishGameEvent(GameEvent.Type type, Vector2i tile) {
        requireNonNull(type);
        publishGameEvent(new GameEvent(this, type, tile));
    }

    // other stuff

    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            Logger.error("{} start tile not specified", ghost.name());
        }
    }

    /**
     * The main logic step of the game. Checks if Pac-Man collides with a ghost or finds food or a bonus.
     * Collision with a ghost either kills the ghost and earns points (in case Pac-Man has power) or kills Pac-Man and
     * loses a life. When Pac-Man finds an energizer pellet he enters power mode and is able to kill ghosts. The duration
     * of the power mode varies between levels.
     */
    protected void doHuntingStep(GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        level.pac().tick(this);
        level.ghosts().forEach(ghost -> ghost.tick(this));
        level.optBonus().ifPresent(bonus -> bonus.tick(this));

        // Ghosts colliding with Pac? While teleportation takes place, collisions are disabled. (Not sure what the
        // original Arcade game does). Collision behavior is controlled by the current collision strategy. The original
        // Arcade games use tile-based collision which can lead to missed collisions by passing through.
        simulationStepResult.ghostsCollidingWithPac.clear();
        level.ghosts()
            .filter(ghost -> !terrain.isTileInPortalSpace(ghost.tile()))
            .filter(ghost -> collisionStrategy().collide(ghost, level.pac()))
            .forEach(simulationStepResult.ghostsCollidingWithPac::add);

        if (!simulationStepResult.ghostsCollidingWithPac.isEmpty()) {
            // Pac killed? Might stay alive when immune or in demo level safe time!
            checkPacKilled(level);
            if (hasPacManBeenKilled()) return;

            // Ghost(s) killed?
            simulationStepResult.ghostsCollidingWithPac.stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(simulationStepResult.killedGhosts::add);
            if (hasGhostBeenKilled()) {
                simulationStepResult.killedGhosts.forEach(this::onEatGhost);
                return;
            }
        }

        checkPacFindsFood(level);
        checkPacFindsBonus(level);

        updatePacPower(level);
        level.blinking().tick();
        level.huntingTimer().update(level.number());
    }

    /**
     * Checks if Pac-Man gets killed by a collision with an attacking ghost.
     * <p>In attract mode (demo level), there is a time interval at the beginning when Pac-Man is safe.
     * This is to avoid having Pac-Man getting killed too early in demo mode.
     * In contrast to the original Arcade games, the demo mode is not fixed but uses random ghost moves so it
     * cannot be predicted how long the demo mode runs.
     * <p>
     * In normal mode, Pac-Man can be made immune against ghost attacks using a cheat command.
     * In this case, Pac-Man is safe against ghost attacks too.
     *
     * @param level the game level
     */
    protected void checkPacKilled(GameLevel level) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && isPacSafeInDemoLevel(level) || !demoLevel && level.pac().isImmune()) {
            return;
        }
        simulationStepResult.pacKiller = simulationStepResult.ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst()
            .orElse(null);
    }

    /**
     * Updates the power of Pac-Man. Power starts fading after some time. When this happens, the ghosts start flashing
     * and when the power timer expires, they take their normal color again and continue chasing Pac-Man.
     *
     * @param level the game level
     */
    protected void updatePacPower(GameLevel level) {
        final Pac pac = level.pac();
        pac.powerTimer().doTick();
        if (pac.isPowerFadingStarting(level)) {
            simulationStepResult.pacStartsLosingPower = true;
            publishGameEvent(GameEvent.Type.PAC_STARTS_LOSING_POWER);
        } else if (pac.powerTimer().hasExpired()) {
            simulationStepResult.pacLostPower = true;
            pac.powerTimer().stop();
            pac.powerTimer().reset(0);
            level.energizerVictims().clear();
            level.huntingTimer().start();
            level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
            publishGameEvent(GameEvent.Type.PAC_LOST_POWER);
        }
    }

    protected void clearCheatingProperties() {
        immuneProperty().set(false);
        usingAutopilotProperty().set(false);
        cheatUsedProperty().set(false);
    }

    protected void updateCheatingProperties(GameLevel level) {
        level.pac().immuneProperty().bind(immuneProperty());
        level.pac().usingAutopilotProperty().bind(usingAutopilotProperty());
        cheatUsedProperty().set(immune() || usingAutopilot());
    }

    // ScoreManager

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Score score = new Score();
    private final Score highScore = new Score();
    private File highScoreFile;
    private Set<Integer> extraLifeScores = Set.of();

    protected void setHighScoreFile(File highScoreFile) {
        this.highScoreFile = requireNonNull(highScoreFile);
    }

    protected void setExtraLifeScores(Integer... scores) {
        extraLifeScores = Set.of(scores);
    }

    protected void scorePoints(int points) {
        if (!score.isEnabled()) {
            return;
        }
        int oldScore = score.points(), newScore = oldScore + points;
        if (highScore().isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(score.levelNumber());
            highScore.setDate(LocalDate.now());
        }
        score.setPoints(newScore);
    }

    protected void loadHighScore() {
        try {
            highScore.read(highScoreFile);
            Logger.info("High Score loaded from file '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
        } catch (IOException x) {
            Logger.error("High Score file could not be opened: '{}'", highScoreFile);
        }
    }

    protected void updateHighScore() {
        var oldHighScore = Score.fromFile(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            try {
                final String dateTime = FORMATTER.format(LocalDateTime.now());
                highScore.save(highScoreFile, "High Score updated at %s".formatted(dateTime));
            } catch (IOException x) {
                Logger.error("High Score file could not be saved: '{}'", highScoreFile);
            }
        }
    }

    public void saveHighScore() {
        try {
            new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
        } catch (IOException x) {
            Logger.error("High Score could not be saved to file '{}'", highScoreFile);
            Logger.error(x);
        }
    }
}