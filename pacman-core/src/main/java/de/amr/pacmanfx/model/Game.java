/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import javafx.beans.property.BooleanProperty;
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
public interface Game extends LevelCounter {

    /* -----------------------------------------------------------
     *  State machine
     * ----------------------------------------------------------- */

    /**
     * Returns the state machine controlling the game flow.
     *
     * @return the {@link GameControl} instance for this game
     */
    GameControl control();

    /**
     * Convenience method for entering a new game state.
     *
     * @param gameState the state to enter
     */
    default void enterState(StateMachine.State<Game> gameState) {
        control().enterState(gameState);
    }

    /**
     * Resumes the previously active state.
     */
    default void resumePreviousState() {
        control().resumePreviousState();
    }

    /**
     * Restarts the given state, reinitializing its timer and internal data.
     *
     * @param gameState the state to restart
     */
    default void restartState(StateMachine.State<Game> gameState) {
        control().restartState(gameState);
    }

    /* -----------------------------------------------------------
     *  Simulation and HUD
     * ----------------------------------------------------------- */

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

    /* -----------------------------------------------------------
     *  Scoring
     * ----------------------------------------------------------- */

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

    /* -----------------------------------------------------------
     *  World map selection
     * ----------------------------------------------------------- */

    /**
     * Returns the map selector that determines which world map is used
     * for each level.
     *
     * @return the {@link WorldMapSelector} for this game
     */
    WorldMapSelector mapSelector();

    /* -----------------------------------------------------------
     *  Game levels
     * ----------------------------------------------------------- */

    /**
     * Returns the current game level, if one is active.
     *
     * @return an {@link Optional} containing the current level
     */
    Optional<GameLevel> optGameLevel();

    /**
     * Convenience method to access the current level.
     *
     * <p>Returns {@code null} if no level is active. Use
     * {@link #optGameLevel()} when you need to handle the absence of a level
     * safely.</p>
     *
     * @return the current {@link GameLevel}, or {@code null} if none exists
     */
    GameLevel level();

    /* -----------------------------------------------------------
     *  Lifecycle
     * ----------------------------------------------------------- */

    /** Initializes the game model before any gameplay begins. */
    void boot();

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

    /** @return {@code true} if the game is currently in a playing state */
    boolean isPlaying();

    /** Sets whether the game is currently being played. */
    void setPlaying(boolean playing);

    /**
     * Continues gameplay after a game‑over or interruption.
     *
     * @param level the level to resume
     * @param tick  the current simulation tick
     */
    void continuePlaying(GameLevel level, long tick);

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

    /** Shows a level message such as “Ready!” or “Game Over”. */
    void showLevelMessage(GameLevelMessageType type);

    /** Clears any active level message. */
    void clearLevelMessage();

    /** @return {@code true} if the given level has been completed */
    boolean isLevelCompleted(GameLevel level);

    /** @return the number of the last completed level */
    int lastLevelNumber();

    /** Starts the next level in sequence. */
    void startNextLevel();

    /* -----------------------------------------------------------
     *  Gameplay flow
     * ----------------------------------------------------------- */

    void startHunting(GameLevel level);
    void whileHunting(GameLevel level);
    void activateNextBonus(GameLevel level);
    boolean isBonusReached(GameLevel level);
    boolean hasPacManBeenKilled();
    boolean hasGhostBeenKilled();

    /* -----------------------------------------------------------
     *  Game event system
     * ----------------------------------------------------------- */

    /** Registers a listener for game events. */
    void addGameEventListener(GameEventListener listener);

    /** Removes a previously registered game event listener. */
    void removeGameEventListener(GameEventListener listener);

    /** Publishes a game event to all registered listeners. */
    void publishGameEvent(GameEvent event);

    /* -----------------------------------------------------------
     *  Event callbacks
     * ----------------------------------------------------------- */

    void onLevelCompleted(GameLevel level);
    void whilePacManDying(GameLevel level, Pac pac, long tick);
    void onEatGhost(GameLevel level, Ghost ghost);
    void whileEatingGhost(GameLevel level, long tick);
    void onGameOver();

    /* -----------------------------------------------------------
     *  Collision strategy
     * ----------------------------------------------------------- */

    /**
     * Returns the collision strategy used to detect interactions between
     * actors (Pac‑Man, ghosts, bonuses, etc.).
     *
     * @return the current {@link CollisionStrategy}
     */
    CollisionStrategy collisionStrategy();

    /** Sets the collision strategy. */
    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    /* -----------------------------------------------------------
     *  Lives
     * ----------------------------------------------------------- */

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

    /* -----------------------------------------------------------
     *  Actor speeds (pixels per tick)
     * ----------------------------------------------------------- */

    float bonusSpeed(GameLevel level);
    float ghostSpeed(GameLevel level, Ghost ghost);
    float pacSpeed(GameLevel level);
    float pacSpeedWhenHasPower(GameLevel level);

    /* -----------------------------------------------------------
     *  Cut scenes
     * ----------------------------------------------------------- */

    /** @return {@code true} if cut scenes are enabled */
    boolean cutScenesEnabled();

    /** Enables or disables cut scenes. */
    void setCutScenesEnabled(boolean enabled);

    /** @return the number of the last cut scene shown */
    int lastCutSceneNumber();

    /* -----------------------------------------------------------
     *  Cheating flags
     * ----------------------------------------------------------- */

    /** Marks that a cheat has been used in this game session. */
    default void raiseCheatFlag() {
        cheatUsedProperty().set(true);
    }

    /** Clears the cheat‑used flag. */
    default void clearCheatFlag() {
        cheatUsedProperty().set(false);
    }

    /** @return property indicating whether a cheat has been used */
    BooleanProperty cheatUsedProperty();

    /** @return property indicating whether Pac‑Man is immune to death */
    BooleanProperty immuneProperty();

    /** @return {@code true} if Pac‑Man is currently immune */
    default boolean isImmune() {
        return immuneProperty().get();
    }

    /** @return property indicating whether autopilot mode is active */
    BooleanProperty usingAutopilotProperty();

    /** @return {@code true} if autopilot is currently active */
    default boolean isUsingAutopilot() {
        return usingAutopilotProperty().get();
    }
}
