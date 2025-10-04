/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;

public interface GameLifecycle {
    void init();
    void resetEverything();
    void prepareForNewGame();
    boolean canStartNewGame();
    void startNewGame();
    boolean isPlaying();
    void setPlaying(boolean playing);
    void continueGame();
    boolean canContinueOnGameOver();
    void buildNormalLevel(int levelNumber);
    void buildDemoLevel();
    void createLevel(int levelNumber, boolean demoLevel);
    void startLevel();
    boolean isLevelCompleted();
    int lastLevelNumber();
    void startNextLevel();
    void startHunting(GameLevel gameLevel);
    void doHuntingStep(GameContext gameContext);
    void activateNextBonus(GameLevel gameLevel);
    boolean hasPacManBeenKilled();
    boolean haveGhostsBeenKilled();
}