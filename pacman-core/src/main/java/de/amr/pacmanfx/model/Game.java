/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.CollisionStrategy;

import java.util.Optional;

public interface Game extends GameLifecycle, GameEvents, ActorSpeedControl {

    GameEventManager      eventManager();
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
}