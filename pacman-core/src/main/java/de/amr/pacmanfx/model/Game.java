/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;

import java.util.Optional;

public interface Game {

    GameStateMachine      stateMachine();
    ScoreManager          scoreManager();
    SimulationStepEvents  simulationStepResults();
    MapSelector           mapSelector();
    Optional<GameLevel>   optGameLevel();
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
    double                pacPowerSeconds(GameLevel level);

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
    void startLevel(GameLevel gameLevel);
    boolean isLevelCompleted(GameLevel gameLevel);
    int lastLevelNumber();
    void startNextLevel();
    void startHunting(GameLevel gameLevel);
    void updateHunting(GameLevel gameLevel);
    void activateNextBonus(GameLevel gameLevel);
    boolean isBonusReached(GameLevel gameLevel);
    boolean hasPacManBeenKilled();
    boolean hasGhostBeenKilled();

    // Game level events
    void onLevelCompleted(GameLevel gameLevel);
    void onPacKilled(GameLevel gameLevel);
    void onGhostKilled(GameLevel gameLevel, Ghost ghost);
    void onGameEnding(GameLevel gameLevel);

    // Actor speed
    float bonusSpeed(GameLevel gameLevel);
    float ghostAttackSpeed(GameLevel level, Ghost ghost);
    float ghostFrightenedSpeed(GameLevel level, Ghost ghost);
    float ghostSpeedInsideHouse(GameLevel level, Ghost ghost);
    float ghostSpeedReturningToHouse(GameLevel level, Ghost ghost);
    float ghostTunnelSpeed(GameLevel level, Ghost ghost);
    float pacNormalSpeed(GameLevel level);
    float pacPowerSpeed(GameLevel level);

    // Game event manager
    void addEventListener(GameEventListener listener);
    void removeEventListener(GameEventListener listener);
    void publishEvent(GameEvent event);
    void publishEvent(GameEventType type);
    void publishEvent(GameEventType type, Vector2i tile);
}