/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

/**
 * Controls which data are shown in Heads-Up Display (HUD).
 */
public interface HUD {

    boolean isVisible();
    void show();
    void hide();

    boolean isLevelCounterVisible();
    void showLevelCounter(boolean visible);

    boolean isLivesCounterVisible();
    void showLivesCounter(boolean visible);

    int visibleLifeCount();
    void setVisibleLifeCount(int count);

    int maxLivesDisplayed();

    boolean isScoreVisible();
    void showScore(boolean visible);

    // Fluent API
    default HUD all(boolean visible) {
        return score(visible).levelCounter(visible).livesCounter(visible);
    }

    default HUD levelCounter(boolean visible) {
        showLevelCounter(visible);
        return this;
    }

    default HUD livesCounter(boolean visible) {
        showLivesCounter(visible);
        return this;
    }

    default HUD score(boolean visible) {
        showScore(visible);
        return this;
    }
}