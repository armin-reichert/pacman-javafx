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
 * Common interface for all Pac-Man game models.
 * <p>
 *
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man details</a>
 */
public interface Game extends LevelCounter {

    /**
     * @return state machine controlling the game
     */
    GameControl control();

    /**
     * Convenience method for changing the game state.
     *
     * @param gameState the new game state
     */
    default void enterState(StateMachine.State<Game> gameState) {
        control().enterState(gameState);
    }

    default void resumePreviousState() {
        control().resumePreviousState();
    }

    default void restartState(StateMachine.State<Game> gameState) {
        control().restartState(gameState);
    }

    /**
     * @return the "frame state", a collection of information on what happened in the current simulation step
     */
    SimulationStep simulationStep();

    /**
     * @return data to be displayed in the heads-up display (HUD)
     */
    HeadsUpDisplay hud();

    /**
     * @return the score of the currently played game
     */
    Score score();

    /**
     * @return the high score of the current game variant
     */
    PersistentScore highScore();

    /**
     * Controls the selection of world maps for the game levels.
     *
     * @return the map selector for this game variant
     */
    WorldMapSelector mapSelector();

    // Game levels

    /**
     * @return the (optional) current game level
     */
    Optional<GameLevel> optGameLevel();

    /**
     * Convenience method to access the current game level.
     * <p>Returns {@code null} if no level is active. Use {@link #optGameLevel()} if you
     * need to handle the absence of a level safely.</p>
     *
     * @return the current {@link GameLevel}, or {@code null} if none exists
     */
    GameLevel level();

    // Lifecycle
    void boot();
    void prepareNewGame();
    boolean canStartNewGame();
    void startNewGame(long tick);
    boolean isPlaying();
    void setPlaying(boolean playing);
    void continuePlaying(GameLevel level, long tick);
    boolean canContinueOnGameOver();
    void buildNormalLevel(int levelNumber);
    void buildDemoLevel();
    GameLevel createLevel(int levelNumber, boolean demoLevel);
    void startLevel(GameLevel level);
    void startDemoLevel(long tick);
    void showLevelMessage(GameLevelMessageType type);
    void clearLevelMessage();
    boolean isLevelCompleted(GameLevel level);
    int lastLevelNumber();
    void startNextLevel();
    void startHunting(GameLevel level);
    void whileHunting(GameLevel level);
    void activateNextBonus(GameLevel level);
    boolean isBonusReached(GameLevel level);
    boolean hasPacManBeenKilled();
    boolean hasGhostBeenKilled();

    // Game event manager
    void addGameEventListener(GameEventListener listener);
    void removeGameEventListener(GameEventListener listener);
    void publishGameEvent(GameEvent event);

    // Game event handling
    void onLevelCompleted(GameLevel level);
    void whilePacManDying(GameLevel level, Pac pac, long tick);
    void onEatGhost(GameLevel level, Ghost ghost);
    void whileEatingGhost(GameLevel level, long tick);
    void onGameOver();

    // Actor collision algorithm
    CollisionStrategy collisionStrategy();
    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    // Pac lives
    int initialLifeCount();
    void setInitialLifeCount(int numLives);

    IntegerProperty lifeCountProperty();
    int lifeCount();
    void addLives(int numLives);

    // Actor speeds in pixels/tick
    float bonusSpeed(GameLevel level);
    float ghostSpeed(GameLevel level, Ghost ghost);
    float pacSpeed(GameLevel level);
    float pacSpeedWhenHasPower(GameLevel level);

    // Cut scenes
    boolean cutScenesEnabled();
    void setCutScenesEnabled(boolean enabled);
    int lastCutSceneNumber();

    // Cheating
    default void raiseCheatFlag() {
        cheatUsedProperty().set(true);
    }
    default void clearCheatFlag() {
        cheatUsedProperty().set(false);
    }
    BooleanProperty cheatUsedProperty();
    BooleanProperty immuneProperty();
    default boolean isImmune() {
        return immuneProperty().get();
    }
    BooleanProperty usingAutopilotProperty();
    default boolean isUsingAutopilot() {
        return usingAutopilotProperty().get();
    }
}