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
    GameData hudData();
    SimulationStep simulationStep();
    ActorSpeedControl actorSpeedControl();
    HuntingTimer huntingTimer();
    MapSelector mapSelector();
    Optional<GateKeeper> gateKeeper();
    Optional<GameLevel> level();

    boolean cutScenesEnabled();
    void setCutScenesEnabled(boolean enabled);
    OptionalInt cutSceneNumber(int levelNumber);

    int     initialLifeCount();
    void    setInitialLifeCount(int numLives);
    int     lifeCount();
    void    addLives(int numLives);
    boolean isPlaying();
    void    setPlaying(boolean playing);
    boolean isLevelCompleted();
    boolean isPacManSafeInDemoLevel();
    int     lastLevelNumber();
    boolean isBonusReached();
    boolean canContinueOnGameOver();
    boolean hasPacManBeenKilled();
    boolean haveGhostsBeenKilled();
    long    pacPowerFadingTicks(GameLevel gameLevel);
    long    pacPowerTicks(GameLevel level);
}