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
 * <p>A {@code Game} encapsulates the complete simulation state and logic of a
 * Pac‑Man variant. It owns the actors, the world map, the scoring system, the
 * level progression, and the finite state machine that governs the game flow.
 * The controller layer interacts with the game exclusively through this
 * interface, without embedding game logic in UI or control code.</p>
 *
 * <p>The design follows the original arcade behavior as documented in:</p>
 * <ul>
 *   <li><a href="https://pacman.holenet.info">Jamey Pittman: The Pac‑Man Dossier</a></li>
 *   <li><a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">
 *       Chad Birch: Understanding Ghost Behavior</a></li>
 *   <li><a href="http://superpacman.com/mspacman/">Ms. Pac‑Man details</a></li>
 * </ul>
 *
 * <p>Implementations are responsible for:</p>
 * <ul>
 *   <li>constructing and maintaining the game state machine</li>
 *   <li>managing level creation and transitions</li>
 *   <li>updating actors and collision logic</li>
 *   <li>publishing game events</li>
 *   <li>tracking score, lives, and HUD information</li>
 * </ul>
 */
public interface Game {

    /**
     * Returns the controller of the game flow.
     *
     * @return the {@link GameFlow} instance for this game
     */
    GameFlow flow();

    GameCheats cheats();

    /**
     * Returns the simulation step information for the current frame.
     * This includes actor movements, collisions, and events that occurred
     * during the most recent update.
     *
     * @return the current {@link SimulationStep}
     */
    SimulationStep simulationStep();

    /**
     * Returns the data used to render the heads‑up display (HUD).
     *
     * @return the {@link HeadsUpDisplay} instance
     */
    HeadsUpDisplay hud();

    /**
     * Returns the counter for the game levels that have been completed including the currently played level.
     *
     * @return the level counter
     */
    LevelCounter levelCounter();

    /**
     * Returns the score of the currently running game.
     *
     * @return the current {@link Score}
     */
    Score score();

    /**
     * Returns the persistent high score for this game variant.
     *
     * @return the {@link PersistentScore} instance
     */
    PersistentScore highScore();

    /**
     * Returns the map selector that determines which world map is used
     * for each level.
     *
     * @return the {@link WorldMapSelector} for this game
     */
    WorldMapSelector mapSelector();

    /**
     * Returns the current game level, if one is active.
     *
     * @return an {@link Optional} containing the current level
     */
    Optional<GameLevel> optGameLevel();

    /**
     * Returns if the game currently is running the demo level.
     *
     * @return {@code true} if demo level is running
     */
    default boolean isDemoLevelRunning() {
        return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
    }

    /** Initializes the game model before any gameplay begins. */
    void init();

    /** Prepares the game for a new play session. */
    void prepareNewGame();

    /**
     * Indicates whether a new game can be started.
     *
     * @return {@code true} if starting a new game is allowed
     */
    boolean canStartNewGame();

    /**
     * Starts a new game at the given simulation tick.
     *
     * @param tick the current simulation tick
     */
    void startNewGame(long tick);

    /** @return {@code true} if the game can continue after game‑over */
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

    /** Starts the given level. */
    void startLevel(GameLevel level);

    /** Starts a demo level at the given tick. */
    void startDemoLevel(long tick);

    /** @return {@code true} if the given level has been completed */
    boolean isLevelCompleted(GameLevel level);

    /** @return the number of the last completed level */
    int lastLevelNumber();

    /** Starts the next level in sequence. */
    void startNextLevel();

    /** @return {@code true} if the game is currently in a playing state */
    boolean isPlayingLevel();

    /** Sets whether the game is currently being played. */
    void setPlayingLevel(boolean playing);

    void onLevelPlayingStart(GameLevel level);

    void doPlayLevel(GameLevel level);

    /**
     * Continues gameplay after a game‑over or interruption.
     *
     * @param level the level to resume
     * @param tick  the current simulation tick
     */
    void continuePlayingLevel(GameLevel level, long tick);

    void activateNextBonus(GameLevel level);

    boolean isBonusReached(GameLevel level);

    boolean hasPacManBeenKilled();

    boolean hasGhostBeenKilled();

    void onLevelCompleted();

    void doPacManDying(Pac pac, long tick);

    void onEatGhost(Ghost ghost);

    void doEatingGhost(long tick);

    void onGameOver();

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

    float bonusSpeed(GameLevel level);

    float ghostSpeed(GameLevel level, Ghost ghost);

    float pacSpeed(GameLevel level);

    float pacSpeedWhenHasPower(GameLevel level);
}
