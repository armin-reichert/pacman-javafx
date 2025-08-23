/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;

import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

public interface Game extends GameLifecycle, GameEvents {
    GameEventManager eventManager();
    ScoreManager scoreManager();
    HUDData hudData();
    SimulationStep simulationStep();
    ActorSpeedControl actorSpeedControl();
    HuntingTimer huntingTimer();
    MapSelector mapSelector();
    Optional<GateKeeper> optGateKeeper();
    Optional<GameLevel> optGameLevel();

    boolean     cutScenesEnabled();
    void        setCutScenesEnabled(boolean enabled);
    OptionalInt optCutSceneNumber(int levelNumber);

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

    default void showMessage(GameLevel gameLevel, MessageType type) {
        requireNonNull(type);
        GameLevelMessage message = new GameLevelMessage(type);
        message.setPosition(gameLevel.defaultMessagePosition());
        gameLevel.setMessage(message);
    }
}