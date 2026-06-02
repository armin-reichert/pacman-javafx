/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.lives.PacManLives;
import de.amr.pacmanfx.model.lives.PacManLivesImpl;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.core.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link GameModel} interface providing the core simulation logic shared by all Pac-Man variants.
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
public abstract class AbstractGameModel implements GameModel {

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(DEFAULT_COLLISION_STRATEGY);

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private final ObjectProperty<GameLevel> level = new SimpleObjectProperty<>();

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    /** Per-tick simulation state (collisions, kills, events). */
    protected final SimulationStep simStep = new SimulationStep();

    protected final Score score = new Score();

    protected PersistentScore highScore;

    protected PacManLives lives;

    protected AbstractGameModel() {
        score.pointsProperty().addListener((_, oldScore, newScore)
            -> handleScoreChange(oldScore.intValue(), newScore.intValue()));

        cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                handleCheatDetected();
            }
        });

        lives = new PacManLivesImpl();
    }

    public void setHighScoreFile(File highScoreFile) {
        requireNonNull(highScoreFile);
        highScore = new PersistentScore(highScoreFile);
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

    @Override
    public void eatPellet(GameLevel level, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);
        scorePoints(rules().pointsForPellet(), level.number());
        if (gateKeeper() != null) {
            gateKeeper().registerFoodEaten(level, level.worldMap().terrainLayer().house());
        }
    }

    /**
     * Called when Pac-Man eats an energizer.
     *
     * @param level the current level
     * @param tile  the tile containing the energizer
     */
    public abstract void eatEnergizer(GameLevel level, Vector2i tile);

    public void eatBonus(GameLevel level, Bonus bonus) {
        scorePoints(bonus.points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.points(), bonus);
        bonus.showEatenForSeconds(rules().eatenBonusDisplaySeconds());
        flow().publishGameEvent(new BonusEatenEvent(this, bonus));
    }

    /**
     * Determines whether Pac-Man is safe from being killed during demo mode.
     *
     * @param demoLevel the demo level
     * @return {@code true} if Pac-Man cannot be killed at this moment
     */
    protected abstract boolean isPacSafeInDemoLevel(GameLevel demoLevel);

    /* -------------------------------------------------------------------------
     * Cheating
     * ---------------------------------------------------------------------- */

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);
    private final BooleanProperty pacImmune = new SimpleBooleanProperty(false);
    private final BooleanProperty pacUsingAutopilot = new SimpleBooleanProperty(false);

    /** @return property indicating whether a cheat has been used */
    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    /** @return property indicating whether Pac‑Man is immune to death */
    public BooleanProperty pacImmuneProperty() {
        return pacImmune;
    }

    /** @return {@code true} if Pac‑Man is currently immune */
    public boolean isPacImmune() {
        return pacImmuneProperty().get();
    }

    /** @return {@code true} if autopilot is currently active */
    public boolean isPacUsingAutopilot() {
        return pacUsingAutopilotProperty().get();
    }

    /** @return property indicating whether autopilot mode is active */
    public BooleanProperty pacUsingAutopilotProperty() {
        return pacUsingAutopilot;
    }

    public void clearCheats() {
        cheatUsed.set(false);
        pacImmune.set(false);
        pacUsingAutopilot.set(false);
    }

    public void updateCheats(GameLevel level) {
        if (level.isDemoLevel() || !level.game().isPlayingLevel()) {
            return;
        }
        final Pac pac = level.entities().pac();
        pac.immuneProperty().set(isPacImmune());
        pac.usingAutopilotProperty().set(isPacUsingAutopilot());
        if (isPacImmune() || isPacUsingAutopilot()) {
            cheatUsed.set(true);
        }
    }

    /* -------------------------------------------------------------------------
     * Game interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public void doLevelPlaying() {
        final GameLevel level = optGameLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);
        doHuntingStep(level);
        if (gateKeeper() != null) {
            gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        updateCheats(level);
    }

    @Override
    public SimulationStep doSimulationStep() {
        return simStep;
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(level.get());
    }

    @Override
    public PacManLives lives() {
        return lives;
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

    /**
     * Returns whether the current level is completed.
     *
     * @return {@code true} if all food has been eaten
     */
    @Override
    public boolean isLevelCompleted() {
        final GameLevel level = optGameLevel().orElseThrow();
        return level.worldMap().foodLayer().remainingFoodCount() == 0;
    }

    /**
     * Called when level starts playing: resets timers, starts animations, and publishes a game event.
     *
     */
    @Override
    public void onStartLevelPlaying() {
        final GameLevel level = optGameLevel().orElseThrow();

        // Clear "READY!" message. "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared!
        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.blinking().setStartState(Pulse.State.ON);
        level.blinking().restart();

        level.entities().pac().animations().playSelected();
        level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());

        final HuntingTimer huntingTimer = level.huntingTimer();
        huntingTimer.startFirstPhase(rules(), level.number());
        flow().publishGameEvent(new HuntingPhaseStartedEvent(this, huntingTimer.phaseIndex(), huntingTimer.currentHuntingPhase()));
    }

    /**
     * Called when a level is completed. Stops timers, resets animations, and clears remaining food.
     */
    @Override
    public void onLevelCompleted(GameLevel level) {
        level.huntingTimer().stop();
        Logger.info("Hunting timer stopped.");

        level.blinking().setStartState(Pulse.State.OFF);
        level.blinking().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.worldMap().foodLayer().eatAll();

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_FULL);
        pac.setSpeed(0);
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        Logger.info("Power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> ghost.animations().stopSelected());
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
        final Vector2f startPosition = level.worldMap().terrainLayer().pacStartPosition();

        final Pac pac = level.entities().pac();
        pac.reset(); // initially invisible!
        pac.setPosition(startPosition);
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetToIndefiniteDuration();
        pac.animations().resetSelected();

        level.entities().ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            final Direction startDir = level.worldMap().terrainLayer().house().ghostStartDirection(ghost.personality());
            ghost.setMoveDir(startDir);
            ghost.setWishDir(startDir);
            ghost.setState(GhostState.LOCKED);
            ghost.animations().resetSelected();
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
     * Handles score changes and awards extra lives when thresholds are crossed.
     *
     * @param oldScore previous score
     * @param newScore new score
     */
    protected void handleScoreChange(int oldScore, int newScore) {
        if (rules().isExtraLifeAwarded(oldScore, newScore)) {
            simStep.extraLifeWon = true;
            simStep.extraLifeScore = newScore;
        }
        if (simStep.extraLifeWon) {
            lives().add(1);
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
        level.blinking().doTick();

        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        final Bonus bonus = level.entities().optBonus().orElse(null);

        boolean quitHunting;
        if (isCollisionDoubleChecked()) {
            quitHunting = evalCollisions(level, pac, ghosts, bonus);
            if (!quitHunting) {
                level.entities().forEach(e -> e.update(level));
                quitHunting = evalCollisions(level, pac, ghosts, bonus);
            }
        } else {
            level.entities().forEach(e -> e.update(level));
            quitHunting = evalCollisions(level, pac, ghosts, bonus);
        }

        if (quitHunting) {
            Logger.info("Hunting has been stopped!");
            return;
        }

        checkFoodFound(level, pac);
        checkBonusFound(level);

        if (!isLevelCompleted()) {
            updatePacPower(level, pac);
            level.huntingTimer().update(rules(), level.number());
        }
    }

    private boolean evalCollisions(GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
        detectCollisions(level, pac, ghosts, bonus);
        if (!simStep.ghostsCollidingWithPac.isEmpty()) {
            // Is Pac getting killed after the collision with a ghost?
            // He might stay alive if immune or in level's safe phase!
            checkPacKilled(level, pac);
            if (hasPacManBeenKilled()) {
                return true;
            }
            else {
                // Frightened ghosts get killed when colliding with Pac
                simStep.ghostsCollidingWithPac.stream()
                    .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                    .forEach(simStep.ghostsKilled::add);
                // More than one ghost might have been killed in this step
                simStep.ghostsKilled.forEach(ghost -> onEatGhost(level, ghost));
                if (hasGhostBeenKilled()) {
                    return true;
                }
            }

            // If collision happened while teleporting (horizontally), move collided actors into visible world
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
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

        return false;
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
            if (rules().isBonusAwarded(level)) {
                activateNextBonus();
                simStep.bonusIndex = level.currentBonusIndex();
            }
            flow().publishGameEvent(new PacEatsFoodEvent(this, pac, simStep.energizerFound, false));
        }
    }

    protected void empowerPac(Pac pac, GameLevel level) {
        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            simStep.pacGotPower = true;
            flow().publishGameEvent(new PacGetsPowerEvent(this, pac));
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

    // Collision behavior is controlled by the current collision strategy.
    // The original Arcade games use tile-based collision which can lead to missed collisions
    // by passing through!
    private void detectCollisions(GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {

        // Ghosts colliding with Pac?
        simStep.ghostsCollidingWithPac.clear();
        ghosts.stream().filter(ghost -> collisionStrategy().collide(pac, ghost))
            .forEach(simStep.ghostsCollidingWithPac::add);

        simStep.edibleBonus = null;
        if (bonus != null && bonus.state() == BonusState.EDIBLE && collisionStrategy().collide(pac, bonus)) {
            simStep.edibleBonus = bonus;
        }

        final Vector2i pacTile = pac.computeTile();
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
     * @param pac   the Pac
     */
    protected void checkPacKilled(GameLevel level, Pac pac) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && isPacSafeInDemoLevel(level) || !demoLevel && pac.isImmune()) {
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
                level.killedGhostsForCurrentEnergizer().clear();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                flow().publishGameEvent(new PacLostPowerEvent(this, pac));
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Score management
     * ---------------------------------------------------------------------- */

    /**
     * Adds points to the current score and updates the high score if necessary.
     *
     * @param points points scored
     * @param levelNumber number of current level
     */
    protected void scorePoints(int points, int levelNumber) {
        if (!score.isEnabled()) {
            return;
        }
        final int oldScore = score.points();
        final int newScore = oldScore + points;
        if (highScore.isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(levelNumber);
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
