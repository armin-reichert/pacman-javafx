/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;

import java.util.Optional;

public interface Game extends GameLifecycle, GameEvents, ActorSpeedControl {
    GameEventManager      eventManager();
    ScoreManager          scoreManager();
    SimulationStepResults simulationStepResults();
    HuntingTimer          huntingTimer();
    MapSelector           mapSelector();
    Optional<GameLevel>   optGameLevel();
    HUD                   hud();

    void                  checkPacFindsFood(GameLevel gameLevel);
    boolean               isBonusReached(GameLevel gameLevel);

    LevelCounter          levelCounter();

    boolean               cutScenesEnabled();
    void                  setCutScenesEnabled(boolean enabled);
    Optional<Integer>     optCutSceneNumber(int levelNumber);

    int                   initialLifeCount();
    void                  setInitialLifeCount(int numLives);
    int                   lifeCount();
    void                  addLives(int numLives);

    double                pacPowerFadingSeconds(GameLevel gameLevel);
    double                pacPowerSeconds(GameLevel level);

    int                   numFlashes(GameLevel gameLevel);
    void                  showMessage(GameLevel gameLevel, MessageType type);
}