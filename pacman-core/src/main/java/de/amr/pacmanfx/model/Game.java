package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Ghost;

import java.util.Optional;
import java.util.OptionalInt;

public interface Game extends ScoreManager {

    SimulationStep simulationStep();

    ActorSpeedControl actorSpeedControl();
    HuntingTimer huntingTimer();
    MapSelector mapSelector();

    boolean areCutScenesEnabled();
    OptionalInt cutSceneNumber(int levelNumber);

    HUD theHUD();
    Optional<GateKeeper> gateKeeper();
    Optional<GameLevel> level();

    void    init();
    void    initAnimationOfPacManAndGhosts();

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
    void    startNewGame(GameContext gameContext);
    void    createLevel(GameContext gameContext, int levelNumber);
    void    buildNormalLevel(GameContext gameContext, int levelNumber);
    void    buildDemoLevel(GameContext gameContext);
    boolean isPacManSafeInDemoLevel();
    void    startLevel();
    void    startNextLevel(GameContext gameContext);
    int     lastLevelNumber();
    boolean continueOnGameOver();

    boolean isBonusReached();
    void    activateNextBonus(GameContext gameContext);

    void    startHunting();
    void    doHuntingStep(GameContext gameContext);

    void    onPacKilled();
    void    onGhostKilled(Ghost ghost);
    void    onLevelCompleted();
    void    onGameEnding();

    boolean hasPacManBeenKilled();
    boolean haveGhostsBeenKilled();
    long    pacPowerFadingTicks(GameLevel gameLevel);
    long    pacPowerTicks(GameLevel level);

}
