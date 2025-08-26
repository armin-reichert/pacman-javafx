/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface Game extends GameLifecycle, GameEvents {
    GameEventManager     eventManager();
    ScoreManager         scoreManager();
    SimulationStep       simulationStep();
    ActorSpeedControl    actorSpeedControl();
    HuntingTimer         huntingTimer();
    MapSelector          mapSelector();
    Optional<GateKeeper> optGateKeeper();
    Optional<GameLevel>  optGameLevel();

    HUDData              hudData();

    void                 clearLevelCounter();
    void                 updateLevelCounter(int levelNumber, byte symbol);
    void                 setLevelCounterEnabled(boolean enabled);
    boolean              levelCounterEnabled();
    List<Byte>           levelCounterSymbols();

    boolean              cutScenesEnabled();
    void                 setCutScenesEnabled(boolean enabled);
    OptionalInt          optCutSceneNumber(int levelNumber);

    int                  initialLifeCount();
    void                 setInitialLifeCount(int numLives);
    int                  lifeCount();
    int                  visibleLifeCount();
    void                 setVisibleLifeCount(int count);
    default int          maxLivesDisplayed() { return 5; }
    void                 addLives(int numLives);
    boolean              isPlaying();
    void                 setPlaying(boolean playing);
    boolean              isLevelCompleted();
    int                  lastLevelNumber();
    boolean              canContinueOnGameOver();
    boolean              hasPacManBeenKilled();
    boolean              haveGhostsBeenKilled();

    double               pacPowerFadingSeconds(GameLevel gameLevel);
    double               pacPowerSeconds(GameLevel level);

    void                 showMessage(GameLevel gameLevel, MessageType type);
}