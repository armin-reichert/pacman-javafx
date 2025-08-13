package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;

import java.util.Optional;
import java.util.OptionalInt;

public interface Game {

    ScoreManager scoreManager();
    SimulationStep simulationStep();

    ActorSpeedControl actorSpeedControl();
    HuntingTimer huntingTimer();
    MapSelector mapSelector();

    boolean areCutScenesEnabled();
    void setCutScenesEnabled(boolean enabled);
    OptionalInt cutSceneNumber(int levelNumber);

    HUD theHUD();
    Optional<GateKeeper> gateKeeper();
    Optional<GameLevel> level();

    void    init();
    void resetPacManAndGhostAnimations();

    int     initialLifeCount();
    void    setInitialLifeCount(int numLives);
    int     lifeCount();
    void    addLives(int numLives);

    boolean isPlaying();
    void    setPlaying(boolean playing);
    boolean isLevelCompleted();

    void    resetEverything();
    void    prepareForNewGame();
    boolean canStartNewGame();
    void    startNewGame();
    void    createLevel(int levelNumber);
    void    buildNormalLevel(int levelNumber);
    void    buildDemoLevel();
    boolean isPacManSafeInDemoLevel();
    void    startLevel();
    void    startNextLevel();
    int     lastLevelNumber();
    boolean continueOnGameOver();

    boolean isBonusReached();
    void    activateNextBonus();

    void    startHunting();
    void    doHuntingStep();

    void    onPacKilled();
    void    onGhostKilled(Ghost ghost);
    void    onLevelCompleted();
    void    onGameEnding();

    boolean hasPacManBeenKilled();
    boolean haveGhostsBeenKilled();
    long    pacPowerFadingTicks(GameLevel gameLevel);
    long    pacPowerTicks(GameLevel level);
}