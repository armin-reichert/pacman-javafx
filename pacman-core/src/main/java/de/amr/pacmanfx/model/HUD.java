/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

/**
 * Controls which data are shown in Heads-Up Display (HUD).
 */
public interface HUD {

    boolean isVisible();
    void show(boolean visible);

    boolean isLevelCounterVisible();
    void showLevelCounter(boolean visible);

    boolean isLivesCounterVisible();
    void showLivesCounter(boolean visible);

    int visibleLifeCount();
    void setVisibleLifeCount(int count);

    boolean isScoreVisible();
    void showScore(boolean visible);

    boolean isCreditVisible();
    void showCredit(boolean visible);

    int numCoins();
    void setNumCoins(int numCoins);

    // Fluent API
    default HUD all(boolean visible) {
        return creditVisible(visible)
            .scoreVisible(visible)
            .levelCounterVisible(visible)
            .livesCounterVisible(visible);
    }

    default HUD creditVisible(boolean visible) {
        showCredit(visible);
        return this;
    }

    default HUD levelCounterVisible(boolean visible) {
        showLevelCounter(visible);
        return this;
    }

    default HUD livesCounterVisible(boolean visible) {
        showLivesCounter(visible);
        return this;
    }

    default HUD scoreVisible(boolean visible) {
        showScore(visible);
        return this;
    }
}