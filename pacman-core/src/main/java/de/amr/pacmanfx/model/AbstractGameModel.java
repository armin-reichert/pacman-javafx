/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.world.TerrainLayer;
import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.halfTileRightOf;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link Game} interface providing the core simulation logic shared by all Pac-Man variants.
 *
 * <p>This abstract model encapsulates:
 * <ul>
 *   <li>the main per-tick simulation loop ({@link #doHuntingStep(GameLevel)})</li>
 *   <li>movement and collision detection for Pac-Man, ghosts, and bonus items</li>
 *   <li>pellet, energizer, and bonus consumption</li>
 *   <li>power mode handling and ghost state transitions</li>
 *   <li>score management, high-score persistence, and extra-life awarding</li>
 *   <li>cheat detection and cheat-related state</li>
 *   <li>event publishing for UI and game control</li>
 * </ul>
 *
 * <p>Concrete game variants (Arcade Pac-Man, Ms. Pac-Man, Tengen, etc.) extend this class and implement
 * variant-specific behavior such as pellet scoring, energizer effects, bonus scoring, and ghost speeds.</p>
 *
 * <p>The model is deterministic and tick-driven: each call to {@link #doHuntingStep(GameLevel)} advances the
 * simulation by one frame.</p>
 */
public abstract class AbstractGameModel implements Game, GameCheats {

    /** Default collision strategy used by the original arcade games. */
    public static final CollisionStrategy DEFAULT_COLLISION_STRATEGY = CollisionStrategy.SAME_TILE;

    // Cheating
    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty immune = new SimpleBooleanProperty(false);
    private final BooleanProperty usingAutopilot = new SimpleBooleanProperty(false);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(DEFAULT_COLLISION_STRATEGY);
    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);
    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);
    private final IntegerProperty initialLifeCount = new SimpleIntegerProperty(3);
    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();
    private final IntegerProperty lifeCount = new SimpleIntegerProperty(0);
    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    /** Per-tick simulation state (collisions, kills, events). */
    protected final SimulationStep simStep = new SimulationStep();

    /** Points awarded for pellets (variant-specific). */
    protected int pelletPoints;

    /** Points awarded for energizers (variant-specific). */
    protected int energizerPoints;

    /** Current score. */
    protected final Score score = new Score();

    /** Persistent high score. */
    protected final PersistentScore highScore;

    /** Score thresholds at which extra lives are awarded. */
    private Set<Integer> extraLifeScores = Set.of();

    /**
     * Creates a new game model with the given high score file.
     *
     * <p>The constructor installs listeners for:
     * <ul>
     *   <li>score changes (to detect extra-life thresholds)</li>
     *   <li>cheat detection (to disable high-score saving)</li>
     * </ul>
     *
     * @param highScoreFile file used for saving and loading the high score
     */
    protected AbstractGameModel(File highScoreFile) {
        requireNonNull(highScoreFile);
        this.highScore = new PersistentScore(highScoreFile);

        score.pointsProperty().addListener((_, oldScore, newScore)
            -> handleScoreChange(oldScore.intValue(), newScore.intValue()));

        cheatUsedProperty().addListener((_, _, cheatDetected) -> {
            if (cheatDetected) {
                handleCheatDetected();
            }
        });
    }

    /**
     * @return property controlling whether collisions are double-checked each tick
     */
    public BooleanProperty collisionDoubleCheckedProperty() {
        return collisionDoubleChecked;
    }

    /**
     * @return {@code true} if collisions are double-checked each tick
     */
    public Boolean isCollisionDoubleChecked() {
        return collisionDoubleCheckedProperty().get();
    }

    /**
     * Enables or disables double collision checking.
     *
     * @param doubleChecked {@code true} to enable double-checking
     */
    public void setCollisionDoubleChecked(boolean doubleChecked) {
        collisionDoubleCheckedProperty().set(doubleChecked);
    }

    /**
     * @return the level property
     */
    public ObjectProperty<GameLevel> levelProperty() {
        return level;
    }

    /* -------------------------------------------------------------------------
     * Variant-specific hooks
     * ---------------------------------------------------------------------- */

    /**
     * Called when Pac-Man eats a normal pellet.
     *
     * @param level the current level
     * @param tile  the tile containing the pellet
     */
    protected abstract void eatPellet(GameLevel level, Vector2i tile);

    /**
     * Called when Pac-Man eats an energizer.
     *
     * @param level the current level
     * @param tile  the tile containing the energizer
     */
    protected abstract void eatEnergizer(GameLevel level, Vector2i tile);

    /**
     * Called when Pac-Man eats a bonus item (fruit).
     *
     * @param level the current level
     * @param bonus the bonus actor
     */
    protected abstract void eatBonus(GameLevel level, Bonus bonus);

    /**
     * Determines whether Pac-Man is safe from being killed during demo mode.
     *
     * @param demoLevel the demo level
     * @return {@code true} if Pac-Man cannot be killed at this moment
     */
    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    /**
     * Returns the ghost's speed while attacking.
     *
     * @param level the current level
     * @param ghost a ghost
     * @return the ghost's speed (pixels per frame) while attacking
     */
    public abstract float ghostSpeedAttacking(GameLevel level, Ghost ghost);

    /**
     * Returns the speed of frightened ghosts.
     *
     * @param level the current level
     * @return the speed of frightened ghosts (pixels per frame)
     */
    public abstract float ghostSpeedWhenFrightened(GameLevel level);

    /**
     * Returns the ghost's speed inside a tunnel for the given level.
     *
     * @param levelNumber the level number
     * @return the ghost's tunnel speed (pixels per frame)
     */
    public abstract float ghostSpeedTunnel(int levelNumber);

    /* -------------------------------------------------------------------------
     * Cheating interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public GameCheats cheats() {
        return this;
    }

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

    /* -------------------------------------------------------------------------
     * Game interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public SimulationStep simulationStep() {
        return simStep;
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
    public PersistentScore highScore() {
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
    public boolean isPlayingLevel() {
        return playing.get();
    }

    @Override
    public void setPlayingLevel(boolean playing) {
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

    /**
     * Returns whether the current level is completed.
     *
     * @param level the current game level
     * @return {@code true} if all food has been eaten
     */
    @Override
    public boolean isLevelCompleted(GameLevel level) {
        return level.worldMap().foodLayer().remainingFoodCount() == 0;
    }

    /**
     * Called when level starts playing: resets timers, starts animations, and publishes a game event.
     *
     * @param level the current level
     */
    @Override
    public void onPlayingLevelStart(GameLevel level) {
        level.huntingTimer().startFirstPhase(level.number());
        level.blinking().setStartState(Pulse.State.ON);
        level.blinking().restart();
        level.pac().playAnimation();
        level.ghosts().forEach(Actor::playAnimation);
        flow().publishGameEvent(new HuntingPhaseStartedEvent(this, level.huntingTimer().phaseIndex(), level.huntingTimer().phase()));
    }

    /**
     * Called when a level is completed. Stops timers, resets animations, and clears remaining food.
     *
     * @param level the completed level
     */
    @Override
    public void onLevelCompleted(GameLevel level) {
        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        level.blinking().setStartState(Pulse.State.OFF);
        level.blinking().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.pac();
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");
        pac.setSpeed(0);
        pac.stopAnimation();
        pac.selectAnimation(Pac.AnimationID.PAC_FULL);

        level.ghosts().forEach(Ghost::stopAnimation);
        level.optBonus().ifPresent(Bonus::setInactive);
    }

    /* -------------------------------------------------------------------------
     * Utility methods
     * ---------------------------------------------------------------------- */

    /**
     * Resets Pac-Man and the ghosts and places them at their start positions in their start states. Pac-Man initially
     * wants to move to the left.
     */
    protected void makeReadyForPlaying(GameLevel level) {
        final Pac pac = level.pac();
        pac.reset(); // initially invisible!
        pac.setPosition(level.worldMap().terrainLayer().pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetToIndefiniteDuration();
        pac.resetAnimation();

        level.ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            final Direction startDir = level.worldMap().terrainLayer().house().ghostStartDirection(ghost.personality());
            ghost.setMoveDir(startDir);
            ghost.setWishDir(startDir);
            ghost.setState(GhostState.LOCKED);
            ghost.resetAnimation();
        });

        level.blinking().setStartState(Pulse.State.ON); // Energizers are visible when ON
        level.blinking().reset();
    }

    /**
     * Sets the start position for the given ghost.
     *
     * @param ghost the ghost
     * @param tile  the start tile (or {@code null} if not specified)
     */
    protected void setGhostStartPosition(Ghost ghost, Vector2i tile) {
        if (tile != null) {
            ghost.setStartPosition(halfTileRightOf(tile));
        } else {
            Logger.error("{} start tile not specified", ghost.name());
        }
    }

    /**
     * Sets the score thresholds at which extra lives are awarded.
     *
     * @param scores the extra life scores (varargs)
     */
    protected void setExtraLifeScores(Integer... scores) {
        extraLifeScores = scores.length <= 1
            ? Set.of(scores) : Collections.unmodifiableSortedSet(new TreeSet<>(Set.of(scores)));
    }

    /**
     * Handles score changes and awards extra lives when thresholds are crossed.
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
            flow().publishGameEvent(new SpecialScoreEvent(this, newScore));
        }
    }

    /**
     * Called when a cheat is detected. Disables high-score saving by default.
     */
    protected void handleCheatDetected() {
        highScore.setEnabled(false);
    }

    /* -------------------------------------------------------------------------
     * Main simulation step
     * ---------------------------------------------------------------------- */

    /**
     * Performs one simulation step of the hunting phase.
     *
     * <p>This method:
     * <ul>
     *   <li>moves Pac-Man and ghosts</li>
     *   <li>detects collisions (ghosts, food, bonus)</li>
     *   <li>handles ghost kills or Pac-Man death</li>
     *   <li>updates power mode and timers</li>
     *   <li>activates bonuses when thresholds are reached</li>
     * </ul>
     *
     * @param level the current game level
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
        level.blinking().doTick();

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

            // If collision happened while teleporting (horizontally), move collided actors into visible world
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            terrain.hPortalContainingTile(pac.tile()).ifPresent(hPortal -> {
                if (pac.moveDir() == Direction.LEFT) {
                    pac.setX(hPortal.rightBorderEntryTile().x() * TS + HTS);
                } else if (pac.moveDir() == Direction.RIGHT) {
                    pac.setX(hPortal.leftBorderEntryTile().x() * TS - HTS);
                }
                // Not sure if colliding ghosts should also be moved back to light
                //simStep.ghostsCollidingWithPac.forEach(ghost -> ghost.setX(pac.x()));
                Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
            });
        }

        if (quitHunting) {
            Logger.info("Hunting has been stopped");
            return;
        }

        checkFoodFound(level, pac);
        checkBonusFound(level);

        if (!isLevelCompleted(level)) {
            updatePacPower(level, pac);
            level.huntingTimer().update(level.number());
        }
    }

    /**
     * Checks whether Pac-Man has entered a tile containing food (pellet or energizer)
     * and triggers the corresponding variant-specific behavior.
     *
     * @param level the current level
     * @param pac   Pac-Man
     */
    private void checkFoodFound(GameLevel level, Pac pac) {
        if (simStep.foodTile == null) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().markFoodEatenAt(simStep.foodTile);
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
            flow().publishGameEvent(new PacEatsFoodEvent(this, pac, false));
        }
    }

    /**
     * Checks whether Pac-Man has collided with an edible bonus item and, if so, triggers
     * the variant-specific bonus handling.
     *
     * @param level the current level
     */
    private void checkBonusFound(GameLevel level) {
        if (simStep.edibleBonus != null) {
            eatBonus(level, simStep.edibleBonus);
        }
    }

    /**
     * Computes and records collisions between Pac-Man, ghosts, food tiles, and bonus items.
     *
     * @param level the current level
     */
    private void detectCollisions(GameLevel level) {
        requireNonNull(level);

        // Ghosts colliding with Pac?
        // Collision behavior is controlled by the current collision strategy. The original Arcade games use
        // tile-based collision which can lead to missed collisions by passing through.
        level.ghosts()
            //.filter(ghost -> !level.worldMap().terrainLayer().isTileInPortalSpace(ghost.tile()))
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
     *
     * <p>In attract mode (demo level), there is a time interval at the beginning when Pac-Man is safe.
     * This is to avoid having Pac-Man getting killed too early in demo mode.
     * In contrast to the original Arcade games, the demo mode is not fixed but uses random ghost moves so it
     * cannot be predicted how long the demo mode runs.</p>
     *
     * <p>In normal mode, Pac-Man can be made immune against ghost attacks using a cheat command.
     * In this case, Pac-Man is safe against ghost attacks too.</p>
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
     * Updates Pac-Man's power mode. Power starts fading after some time. When this happens, the ghosts start flashing
     * and when the power timer expires, they take their normal color again and continue chasing Pac-Man.
     *
     * @param level the game level
     * @param pac the Pac-Man
     */
    protected void updatePacPower(GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                simStep.pacStartsLosingPower = true;
                flow().publishGameEvent(new PacPowerFadesEvent(this, pac));
            } else if (pac.powerTimer().hasExpired()) {
                simStep.pacLostPower = true;
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.energizerVictims().clear();
                level.huntingTimer().start();
                level.ghosts(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                flow().publishGameEvent(new PacLostPowerEvent(this, pac));
            }
        }
    }

    /**
     * Clears all cheating-related properties.
     */
    protected void clearCheatingProperties() {
        immuneProperty().set(false);
        usingAutopilotProperty().set(false);
        cheats().clearFlag();
    }

    /**
     * Updates cheating properties in the current level by binding Pac-Man's properties
     * to the global cheat flags and raising the cheat flag if any cheat is active.
     *
     * @param level the current game level
     */
    protected void updateCheatingProperties(GameLevel level) {
        level.pac().immuneProperty().bind(immuneProperty());
        level.pac().usingAutopilotProperty().bind(usingAutopilotProperty());
        if (cheats().isImmune() || cheats().isUsingAutopilot()) {
            cheats().raiseFlag();
        }
    }

    /* -------------------------------------------------------------------------
     * Score management
     * ---------------------------------------------------------------------- */

    /**
     * Adds points to the current score and updates the high score if necessary.
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
     * Updates the high score file if the current high score is higher than the saved one.
     *
     * @throws IOException if saving fails
     */
    protected void updateHighScore() throws IOException {
        final PersistentScore savedHighScore = new PersistentScore(highScore.file());
        savedHighScore.load();
        if (highScore.points() > savedHighScore.points()) {
            highScore.save();
        }
    }
}
