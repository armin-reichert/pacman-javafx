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
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;

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

    StateMachine<FsmState<GameContext>, GameContext> stateMachine();

    default FsmState<GameContext> stateByName(String name) {
        return stateMachine().state(name);
    }

    default FsmState<GameContext> state() {
        return stateMachine().state();
    }

    default void changeState(FsmState<GameContext> gameState) {
        stateMachine().changeState(gameState);
    }

    default void changeState(String gameStateID) {
        stateMachine().changeState(stateMachine().state(gameStateID));
    }

    default void restart(FsmState<GameContext> gameState) {
        stateMachine().restart(gameState);
    }

    default void resumePreviousState() {
        stateMachine().resumePreviousState();
    }

    default void restart(String gameStateID) {
        stateMachine().restart(stateMachine().state(gameStateID));
    }

    default void terminateCurrentGameState() {
        stateMachine().state().timer().expire();
    }

    SimulationStepResult  simulationStepResult();

    ScoreManager scoreManager();
    MapSelector mapSelector();

    Optional<GameLevel> optGameLevel();

    /**
     * Convenience method to access the current game level.
     * <p>Returns {@code null} if no level is active. Use {@link #optGameLevel()} if you
     * need to handle the absence of a level safely.</p>
     *
     * @return the current {@link GameLevel}, or {@code null} if none exists
     */
    GameLevel level();

    LevelCounter          levelCounter();
    HUD                   hud();

    boolean               cutScenesEnabled();
    void                  setCutScenesEnabled(boolean enabled);
    Optional<Integer>     optCutSceneNumber(int levelNumber);

    int                   initialLifeCount();
    void                  setInitialLifeCount(int numLives);
    int                   lifeCount();
    void                  addLives(int numLives);

    CollisionStrategy     collisionStrategy();
    void                  setCollisionStrategy(CollisionStrategy collisionStrategy);
    boolean               actorsCollide(Actor either, Actor other);

    double                pacPowerFadingSeconds(GameLevel gameLevel);
    double                pacPowerSeconds(GameLevel gameLevel);

    int                   numFlashes(GameLevel gameLevel);
    void                  showMessage(GameLevel gameLevel, MessageType type);

    // Lifecycle
    void init();
    void resetEverything();
    void prepareForNewGame();
    boolean canStartNewGame();
    void startNewGame();
    boolean isPlaying();
    void setPlaying(boolean playing);
    void continueGame(GameLevel gameLevel);
    boolean canContinueOnGameOver();
    void buildNormalLevel(int levelNumber);
    void buildDemoLevel();
    GameLevel createLevel(int levelNumber, boolean demoLevel);
    void startLevel();
    boolean isLevelCompleted();
    int lastLevelNumber();
    void startNextLevel();
    void startHunting(GameLevel gameLevel);
    void updateHunting(GameLevel gameLevel);
    void activateNextBonus(GameLevel gameLevel);
    boolean isBonusReached(GameLevel gameLevel);
    boolean hasPacManBeenKilled();
    boolean hasGhostBeenKilled();

    // Game event handling
    void onLevelCompleted();
    void onPacKilled(GameLevel gameLevel);
    void onGhostKilled(GameLevel gameLevel, Ghost ghost);
    void onGameEnding(GameLevel gameLevel);

    // Actor speeds in pixels/tick
    float bonusSpeed(GameLevel gameLevel);
    float ghostSpeedWhenAttacking(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedWhenFrightened(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedInsideHouse(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedInsideTunnel(GameLevel gameLevel, Ghost ghost);
    float ghostSpeedReturningToHouse(GameLevel gameLevel, Ghost ghost);
    float pacSpeed(GameLevel gameLevel);
    float pacSpeedWhenHasPower(GameLevel gameLevel);

    // Game event manager
    void addGameEventListener(GameEventListener listener);
    void removeGameEventListener(GameEventListener listener);
    void publishGameEvent(GameEvent event);
    void publishGameEvent(GameEvent.Type type);
    void publishGameEvent(GameEvent.Type type, Vector2i tile);
}