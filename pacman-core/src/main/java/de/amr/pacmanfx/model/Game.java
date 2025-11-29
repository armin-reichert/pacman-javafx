/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.Optional;

/**
 * Common interface for all Pac-Man game models.
 * <p>
 * For more information:
 *
 * @see <a href="https://pacman.holenet.info">Jamey Pittman: The Pac-Man Dossier</a>
 * @see <a href="https://gameinternals.com/understanding-pac-man-ghost-behavior">Chad Birch: Understanding ghost
 * behavior</a>
 * @see <a href="http://superpacman.com/mspacman/">Ms. Pac-Man details</a>
 */
public interface Game {

    // State machine controlling game play
    StateMachine<FsmState<GameContext>, GameContext> stateMachine();

    default FsmState<GameContext> state() {
        return stateMachine().state();
    }

    default void changeState(FsmState<GameContext> gameState) {
        stateMachine().changeState(gameState);
    }

    default void changeState(String stateID) {
        Optional<FsmState<GameContext>> optState = stateMachine().optState(stateID);
        optState.ifPresentOrElse(state -> stateMachine().changeState(state),
            () -> Logger.error("Cannot change state to '{}'. State not existing.", stateID));
    }

    default void restart(FsmState<GameContext> gameState) {
        stateMachine().restart(gameState);
    }

    default void resumePreviousState() {
        stateMachine().resumePreviousState();
    }

    default void restart(String stateID) {
        Optional<FsmState<GameContext>> optState = stateMachine().optState(stateID);
        optState.ifPresentOrElse(state -> stateMachine().restart(state),
            () -> Logger.error("Cannot restart in state to '{}'. State not existing.", stateID));
    }

    default void terminateCurrentGameState() {
        stateMachine().state().timer().expire();
    }

    SimulationStepResult  simulationStepResult();

    HUD hud();
    ScoreManager scoreManager();
    MapSelector mapSelector();

    // Game levels
    Optional<GameLevel> optGameLevel();

    /**
     * Convenience method to access the current game level.
     * <p>Returns {@code null} if no level is active. Use {@link #optGameLevel()} if you
     * need to handle the absence of a level safely.</p>
     *
     * @return the current {@link GameLevel}, or {@code null} if none exists
     */
    GameLevel level();

    LevelCounter levelCounter();

    // Lifecycle
    void init();
    void resetEverything();
    void prepareForNewGame();
    boolean canStartNewGame();
    void startNewGame();
    boolean isPlaying();
    void setPlaying(boolean playing);
    void continueGame();
    boolean canContinueOnGameOver();
    void buildNormalLevel(int levelNumber);
    void buildDemoLevel();
    GameLevel createLevel(int levelNumber, boolean demoLevel);
    void startLevel();
    boolean isLevelCompleted();
    int lastLevelNumber();
    void startNextLevel();
    void startHunting();
    void updateHunting();
    void activateNextBonus();
    boolean isBonusReached();
    boolean hasPacManBeenKilled();
    boolean hasGhostBeenKilled();

    // Game event manager
    void addGameEventListener(GameEventListener listener);
    void removeGameEventListener(GameEventListener listener);
    void publishGameEvent(GameEvent event);
    void publishGameEvent(GameEvent.Type type);
    void publishGameEvent(GameEvent.Type type, Vector2i tile);

    // Game event handling
    void onLevelCompleted();
    void onPacKilled();
    void onGhostKilled(Ghost ghost);
    void onGameEnding();

    // Actor collision algorithm
    CollisionStrategy collisionStrategy();
    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    // Pac lives
    int initialLifeCount();
    void setInitialLifeCount(int numLives);
    int lifeCount();
    void addLives(int numLives);

    // Pac power
    double pacPowerFadingSeconds(GameLevel gameLevel);
    double pacPowerSeconds(GameLevel gameLevel);

    // Actor speeds in pixels/tick
    float bonusSpeed(GameLevel gameLevel);
    float ghostSpeedWhenAttacking(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedWhenFrightened(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedInsideHouse(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedInsideTunnel(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedReturningToHouse(GameLevel gameLevel, Ghost ghost);
    float pacSpeed(GameLevel gameLevel);
    float pacSpeedWhenHasPower(GameLevel gameLevel);

    // Cut scenes
    boolean cutScenesEnabled();
    void setCutScenesEnabled(boolean enabled);
}