/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import javafx.beans.property.IntegerProperty;

import java.util.Optional;

/**
 * Common interface for all Pac‑Man game models.
 *
 * <p>A {@code Game} represents the complete simulation state and logic of a
 * Pac‑Man variant. It owns all core subsystems: actors, world map, scoring,
 * level progression, and the finite state machine that drives the game flow.
 * The controller layer interacts with the game exclusively through this
 * interface, ensuring that UI and input code remain free of game logic.</p>
 *
 * <p>The behavior of the game model follows the original arcade implementations,
 * as documented in:</p>
 * <ul>
 *   <li><a href="https://pacman.holenet.info">Jamey Pittman: The Pac‑Man Dossier</a></li>
 *   <li><a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">
 *       Chad Birch: Understanding Ghost Behavior</a></li>
 *   <li><a href="http://superpacman.com/mspacman/">Ms. Pac‑Man details</a></li>
 * </ul>
 *
 * <p>Implementations of this interface are responsible for:</p>
 * <ul>
 *   <li>constructing and maintaining the game state machine</li>
 *   <li>managing level creation, transitions, and progression</li>
 *   <li>updating actors and applying collision logic</li>
 *   <li>publishing game events to observers</li>
 *   <li>tracking score, lives, and HUD information</li>
 * </ul>
 */
public interface Game {

    /** @return the controller responsible for managing the game flow */
    GameFlow flow();

    /** @return cheat configuration for this game */
    GameCheats cheats();

    /**
     * Returns the simulation step information for the current frame.
     *
     * @return the current {@link SimulationStep}
     */
    SimulationStep simulationStep();

    /** @return the data used to render the heads‑up display (HUD) */
    HeadsUpDisplay hud();

    /** @return the counter tracking completed and current levels */
    LevelCounter levelCounter();

    /** @return the score of the current play session */
    Score score();

    /** @return the persistent high score for this game variant */
    PersistentScore highScore();

    /** @return the map selector determining which maze is used per level */
    WorldMapSelector mapSelector();

    /** @return the current level, if one is active */
    Optional<GameLevel> optGameLevel();

    /** @return {@code true} if the current level is a demo level */
    default boolean isDemoLevelRunning() {
        return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
    }

    /** Initializes the game model before any gameplay begins. */
    void init();

    /** Prepares the game for a new play session. */
    void prepareNewGame();

    /** @return {@code true} if a new game may be started */
    boolean canStartNewGame();

    /**
     * Starts a new game at the given simulation tick.
     *
     * @param tick the current simulation tick
     */
    void startNewGame(long tick);

    /** @return {@code true} if the game may continue after game‑over */
    boolean canContinueOnGameOver();

    /** Builds a normal (non‑demo) level. */
    void buildNormalLevel(int levelNumber);

    /** Builds a demo level used for attract mode. */
    void buildDemoLevel();

    /**
     * Creates a new level instance.
     *
     * @param levelNumber the level number
     * @param demoLevel   whether this is a demo level
     * @return the created {@link GameLevel}
     */
    GameLevel createLevel(int levelNumber, boolean demoLevel);

    /** Starts the current level. */
    void startLevel();

    /** Starts a demo level at the given tick. */
    void startDemoLevel(long tick);

    /** @return {@code true} if the current level has been completed */
    boolean isLevelCompleted();

    /** @return the number of the last completed level */
    int lastLevelNumber();

    /** Starts the next level in sequence. */
    void startNextLevel();

    /** @return {@code true} if the game is currently in a playing state */
    boolean isPlayingLevel();

    /** Sets whether the game is currently being played. */
    void setPlayingLevel(boolean playing);

    /** Called when level gameplay begins. */
    void onStartLevelPlaying();

    /** Performs the main update logic while the level is being played. */
    void doLevelPlaying();

    /**
     * Continues level gameplay at the given tick.
     *
     * @param tick the current simulation tick
     */
    void continuePlayingLevel(long tick);

    /** Activates the next bonus item, if applicable. */
    void activateNextBonus();

    /** @return {@code true} if the bonus threshold has been reached */
    boolean isBonusReached();

    /** @return {@code true} if Pac‑Man has been killed this frame */
    boolean hasPacManBeenKilled();

    /** @return {@code true} if a ghost has been killed this frame */
    boolean hasGhostBeenKilled();

    /** Called when the level has been completed. */
    void onLevelCompleted();

    /**
     * Handles Pac‑Man's death sequence.
     *
     * @param pac  the Pac‑Man actor
     * @param tick the current simulation tick
     */
    void doPacManDying(Pac pac, long tick);

    /**
     * Called when Pac‑Man eats a ghost.
     *
     * @param ghost the ghost that was eaten
     */
    void onEatGhost(Ghost ghost);

    /**
     * Performs the logic for the ghost‑eaten sequence.
     *
     * @param tick the current simulation tick
     */
    void doEatingGhost(long tick);

    /** Called when the game reaches the game‑over state. */
    void onGameOver();

    /** @return the number of the last intermission shown */
    int lastIntermissionNumber();

    /**
     * Returns the collision strategy used to detect interactions between
     * actors (Pac‑Man, ghosts, bonuses, etc.).
     *
     * @return the current {@link CollisionStrategy}
     */
    CollisionStrategy collisionStrategy();

    /** Sets the collision strategy. */
    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    /** @return the number of lives the player starts with */
    int initialLifeCount();

    /** Sets the initial number of lives. */
    void setInitialLifeCount(int numLives);

    /** @return the property tracking the current life count */
    IntegerProperty lifeCountProperty();

    /** @return the current number of lives */
    int lifeCount();

    /** Adds the given number of lives. */
    void addLives(int numLives);

    /**
     * Returns Pac‑Man's bonus speed for the given level.
     *
     * @param level the current level
     * @return the bonus speed multiplier
     */
    float bonusSpeed(GameLevel level);

    /**
     * Returns the speed of the given ghost for the given level.
     *
     * @param level the current level
     * @param ghost the ghost whose speed is requested
     * @return the ghost's speed multiplier
     */
    float ghostSpeed(GameLevel level, Ghost ghost);

    /**
     * Returns Pac‑Man's normal speed for the given level.
     *
     * @param level the current level
     * @return Pac‑Man's speed multiplier
     */
    float pacSpeed(GameLevel level);

    /**
     * Returns Pac‑Man's speed while he has power (after eating a power pellet).
     *
     * @param level the current level
     * @return Pac‑Man's powered‑up speed multiplier
     */
    float pacSpeedWhenHasPower(GameLevel level);
}
