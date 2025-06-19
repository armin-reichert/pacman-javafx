/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public interface HUD {

    boolean isVisible();
    void show();
    void hide();

    LevelCounter levelCounter();
    LivesCounter livesCounter();

    boolean isLevelCounterVisible();
    void showLevelCounter();
    void hideLevelCounter();

    boolean isLivesCounterVisible();
    void showLivesCounter();
    void hideLivesCounter();

    boolean isScoreVisible();
    void showScore();
    void hideScore();
}
