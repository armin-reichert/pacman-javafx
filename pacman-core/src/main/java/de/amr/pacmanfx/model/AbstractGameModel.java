/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.*;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all Pac-Man game models. Any logic implemented here should be identical in all Pac-Man game variants.
 */
public abstract class AbstractGameModel implements Game {

    private static final DateTimeFormatter SCORE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final CollisionStrategy DEFAULT_COLLISION_STRATEGY = CollisionStrategy.SAME_TILE;

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(DEFAULT_COLLISION_STRATEGY);

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    private final BooleanProperty immune = new SimpleBooleanProperty(false);

    private final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);

    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();

    private final IntegerProperty lifeCount = new SimpleIntegerProperty(0);

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);

    protected GameControl gameControl;

    protected final SimulationStep simStep = new SimulationStep();

    protected int pelletPoints;
    protected int energizerPoints;

    protected final Score score = new Score();
    protected final Score highScore = new Score();

    private Set<Integer> extraLifeScores = Set.of();

    protected final File highScoreFile;

    /**
     * Creates a new game model with the given high score file.
     *
     * @param highScoreFile file for saving/loading high score
     */
    protected AbstractGameModel(File highScoreFile) {
        this.highScoreFile = requireNonNull(highScoreFile);

        score.pointsProperty().addListener((_, oldScore, newScore)
                -> handleScoreChange(oldScore.intValue(), newScore.intValue()));

        cheatUsedProperty().addListener((_, _, cheatDetected) -> {
            if (cheatDetected) {
                handleCheatDetected();
            }
        });
    }

    /**
     * Sets the game control (state machine) for this model.
     *
     * @param gameControl the game control
     */
    public void setGameControl(GameControl gameControl) {
        this.gameControl = requireNonNull(gameControl);
        gameControl.stateMachine().setContext(this);
        gameControl.stateMachine().addStateChangeListener(
                (oldState, newState) -> publishGameEvent(new GameStateChangeEvent(oldState, newState)));
    }

    public BooleanProperty collisionDoubleCheckedProperty() {
        return collisionDoubleChecked;
    }

    /**
     * @return true if collisions are double-checked each tick (to reduce missed collisions)
     */
    public Boolean isCollisionDoubleChecked() {
        return collisionDoubleCheckedProperty().get();
    }

    /**
     * Enables/disables double-checking of collisions each tick.
     *
     * @param doubleChecked true to enable double-checking
     */
    public void setCollisionDoubleChecked(boolean doubleChecked) {
        collisionDoubleCheckedProperty().set(doubleChecked);
    }

    public ObjectProperty<GameLevel> levelProperty() {
        return level;
    }

    // To be implemented by subclasses

    /**
     * Called when Pac-Man finds a normal (non-energizer) pellet.
     *
     * @param level the current game level
     * @param tile  the tile containing the pellet
     */
    protected abstract void eatPellet(GameLevel level, Vector2i tile);

    /**
     * Called when Pac-Man finds an energizer pellet.
     *
     * @param level the current game level
     * @param tile  the tile containing the energizer
     */
    protected abstract void eatEnergizer(GameLevel level, Vector2i tile);

    /**
     * Called when Pac-Man collides with a bonus actor.
     *
     * @param level the current game level
     * @param bonus the bonus actor
     */
    protected abstract void eatBonus(GameLevel level, Bonus bonus);

    /**
     * @param demoLevel the running demo level
     * @return {@code true} if Pac-Man cannot be killed in this demo level at the current time
     */
    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    // These methods are public so the info panel can call them

    /**
     * @param level the current game level
     * @param ghost a ghost
     * @return the attack speed (pixels per frame) of this ghost
     */
    public abstract float ghostSpeedAttacking(GameLevel level, Ghost ghost);

    /**
     * @param level the current game level
     * @return the speed (pixels per frame) of frightened ghosts
     */
    public abstract float ghostSpeedWhenFrightened(GameLevel level);

    /**
     * @param levelNumber the game level number (1, 2, ...)
     * @return the speed (pixels per frame) of a ghost inside a tunnel leading to a portal
     */
    public abstract float ghostSpeedTunnel(int levelNumber);

    // Game interface implementation

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
    public SimulationStep simulationStep() {
        return simStep;
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
    public boolean isPlaying() {
        return playing.get();
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public boolean hasPacManBeenKilled() {
        return simStep.pacKiller != null;
    }

    @Override
    public boolean hasGhostBeenKilled() {
        return !simStep.ghostsKilled.isEmpty();
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

        level.blinking().setStartState(Pulse.State.ON);
        level.blinking().restart();
        level.pac().optAnimationManager().ifPresent(AnimationManager::play);
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));

        publishGameEvent(new HuntingPhaseStartedEvent(level.huntingTimer().phaseIndex(), level.huntingTimer().phase()));
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
        pac.stopAnimation();
        pac.selectAnimation(Pac.AnimationID.PAC_FULL);
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
        Logger.trace("Publish game event: {}", event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }

    // other stuff

    /**
     * Sets the start position for the given ghost.
     *
     * @param ghost the ghost
     * @param tile  the start tile (or null if not specified)
     */
    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            Logger.error("{} start tile not specified", ghost.name());
        }
    }

    /**
     * Sets the scores at which extra lives are awarded.
     *
     * @param scores the extra life scores (varargs)
     */
    protected void setExtraLifeScores(Integer... scores) {
        extraLifeScores = scores.length <= 1
                ? Set.of(scores) : Collections.unmodifiableSortedSet(new TreeSet<>(Set.of(scores)));
    }

    /**
     * Handles score changes, checking for extra life awards.
     *
     * @param oldScore previous score
     * @param newScore new score
     */
    protected void handleScoreChange(int oldScore, int newScore) {
        for (int extraLifeScore : extraLifeScores) {
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                simStep.extraLifeWon = true;
                simStep.extraLifeScore = extraLifeScore;
                break;
            }
        }
        if (simStep.extraLifeWon) {
            addLives(1);
            publishGameEvent(new SpecialScoreReachedEvent(newScore));
        }
    }

    /**
     * Called when a cheat is detected. Disables high score saving by default.
     */
    protected void handleCheatDetected() {
        highScore.setEnabled(false);
    }

    /**
     * The main logic step of all Pac-Man games. Checks if Pac-Man collides with a ghost or finds food or a bonus.
     * Collision with a ghost either kills the ghost and earns points (in case Pac-Man has power) or kills Pac-Man and
     * loses a life. When Pac-Man finds an energizer pellet he enters power mode and is able to kill ghosts. The duration
     * of the power mode varies between levels.
     *
     * @param level the game level
     */
    protected void doHuntingStep(GameLevel level) {
        final Pac pac = level.pac();
        boolean quitHunting = false;

        pac.tick(this);
        detectCollisions(level);

        level.ghosts().forEach(ghost -> ghost.tick(this));

        if (isCollisionDoubleChecked()) {
            // call collision detection 2nd time, this should minimize collision missing
            detectCollisions(level);
        }

        level.optBonus().ifPresent(bonus -> bonus.tick(this));
        level.blinking().tick();

        if (!simStep.ghostsCollidingWithPac.isEmpty()) {
            // Is Pac getting killed after the collision with a ghost?
            // He might stay alive if immune or in level's safe phase!
            checkPacKilled(level);
            if (hasPacManBeenKilled()) {
                quitHunting = true;
            }
            else {
                // Frightened ghosts get killed when colliding with Pac
                simStep.ghostsCollidingWithPac.stream()
                        .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                        .forEach(simStep.ghostsKilled::add);
                // More than one ghost might have been killed in this step
                simStep.ghostsKilled.forEach(ghost -> onEatGhost(level, ghost));
                if (hasGhostBeenKilled()) {
                    quitHunting = true;
                }
            }
        }

        if (quitHunting) {
            Logger.info("Hunting has been stopped");
            return;
        }

        checkFoodFound(level, pac);
        checkBonusFound(level);

        if (!isLevelCompleted()) {
            updatePacPower(level);
            level.huntingTimer().update(level.number());
        }
    }

    // Did Pac enter a tile containing a pellet or an energizer?
    private void checkFoodFound(GameLevel level, Pac pac) {
        if (simStep.foodTile == null) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().registerFoodEatenAt(simStep.foodTile);
            pac.endStarving();
            if (simStep.energizerFound) {
                eatEnergizer(level, simStep.foodTile);
            } else {
                eatPellet(level, simStep.foodTile);
            }
            if (isBonusReached(level)) {
                activateNextBonus(level);
                simStep.bonusIndex = level.currentBonusIndex();
            }
            publishGameEvent(new PacEatsFoodEvent(pac, false));
        }
    }

    private void checkBonusFound(GameLevel level) {
        if (simStep.edibleBonus != null) {
            eatBonus(level, simStep.edibleBonus);
        }
    }

    // Compute and record collisions between actors and Pac-Man/food collisions
    private void detectCollisions(GameLevel level) {
        requireNonNull(level);

        // Ghosts colliding with Pac? While teleportation takes place, collisions are disabled. (Not sure what the
        // original Arcade game does). Collision behavior is controlled by the current collision strategy. The original
        // Arcade games use tile-based collision which can lead to missed collisions by passing through.
        level.ghosts()
                .filter(ghost -> !level.worldMap().terrainLayer().isTileInPortalSpace(ghost.tile()))
                .filter(ghost -> collisionStrategy().collide(level.pac(), ghost))
                .forEach(simStep.ghostsCollidingWithPac::add);

        simStep.edibleBonus = level.optBonus()
                .filter(bonus -> bonus.state() == BonusState.EDIBLE)
                .filter(bonus -> collisionStrategy().collide(level.pac(), bonus))
                .orElse(null);

        final Vector2i pacTile = level.pac().tile();
        if (level.worldMap().foodLayer().hasFoodAtTile(pacTile)) {
            simStep.foodTile = pacTile;
            simStep.energizerFound = level.worldMap().foodLayer().isEnergizerTile(pacTile);
        }
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
        simStep.pacKiller = simStep.ghostsCollidingWithPac.stream()
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
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                simStep.pacStartsLosingPower = true;
                publishGameEvent(new PacStartsLosingPowerEvent());
            } else if (pac.powerTimer().hasExpired()) {
                simStep.pacLostPower = true;
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.energizerVictims().clear();
                level.huntingTimer().start();
                level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                publishGameEvent(new PacLostPowerEvent());
            }
        }
    }

    /**
     * Clears all cheating-related properties.
     */
    protected void clearCheatingProperties() {
        immuneProperty().set(false);
        usingAutopilotProperty().set(false);
        clearCheatFlag();
    }

    /**
     * Updates cheating properties in the current level.
     *
     * @param level the current game level
     */
    protected void updateCheatingProperties(GameLevel level) {
        level.pac().immuneProperty().bind(immuneProperty());
        level.pac().usingAutopilotProperty().bind(usingAutopilotProperty());
        if (immune() || usingAutopilot()) {
            raiseCheatFlag();
        }
    }

    // ScoreManager

    /**
     * Adds points to the current score and updates high score if necessary.
     *
     * @param level  the current game level
     * @param points points to add
     */
    protected void scorePoints(GameLevel level, int points) {
        if (!score.isEnabled()) {
            return;
        }
        final int oldScore = score.points();
        final int newScore = oldScore + points;
        if (highScore.isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(level.number());
            highScore.setDate(LocalDate.now());
        }
        score.setPoints(newScore);
    }

    /**
     * Loads the high score from file.
     *
     * @throws IOException if loading fails
     */
    protected void loadHighScore() throws IOException {
        highScore.read(highScoreFile);
        Logger.info("High Score loaded from file '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
    }

    /**
     * Updates the high score file if the current high score is higher than the saved one.
     *
     * @throws IOException if saving fails
     */
    protected void updateHighScore() throws IOException {
        final Score savedHighScore = Score.fromFile(highScoreFile);
        if (highScore.points() > savedHighScore.points()) {
            saveHighScore();
        }
    }

    /**
     * Saves the current high score to file.
     */
    public void saveHighScore() {
        try {
            final String dateTime = SCORE_DATE_TIME_FORMATTER.format(LocalDateTime.now());
            highScore.save(highScoreFile, "High Score updated at %s".formatted(dateTime));
        } catch (IOException x) {
            Logger.error(x, "High Score could not be saved to file '{}'", highScoreFile);
        }
    }
}