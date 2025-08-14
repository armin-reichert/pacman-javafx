/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.ActorSpeedControl;

import java.util.Optional;
import java.util.OptionalInt;

public interface Game extends GameLifecycle, GameEvents {
    ScoreManager scoreManager();
    HUDData hudData();
    SimulationStep simulationStep();
    ActorSpeedControl actorSpeedControl();
    HuntingTimer huntingTimer();
    MapSelector mapSelector();
    Optional<GateKeeper> gateKeeper();
    Optional<GameLevel> level();

    boolean     cutScenesEnabled();
    void        setCutScenesEnabled(boolean enabled);
    OptionalInt cutSceneNumber(int levelNumber);

    int     initialLifeCount();
    void    setInitialLifeCount(int numLives);
    int     lifeCount();
    void    addLives(int numLives);
    boolean isPlaying();
    void    setPlaying(boolean playing);
    boolean isLevelCompleted();
    int     lastLevelNumber();
    boolean canContinueOnGameOver();
    boolean hasPacManBeenKilled();
    boolean haveGhostsBeenKilled();

    double pacPowerFadingSeconds(GameLevel gameLevel);
    double pacPowerSeconds(GameLevel level);
}