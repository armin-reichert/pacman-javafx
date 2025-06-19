/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public interface HUD {

    boolean isVisible();
    void show(boolean b);

    LevelCounter levelCounter();
    LivesCounter livesCounter();

    boolean isLevelCounterVisible();
    void showLevelCounter(boolean b);

    boolean isLivesCounterVisible();
    void showLivesCounter(boolean b);

    boolean isScoreVisible();
    void showScore(boolean b);

    //boolean isCreditVisible();
}
