/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

/**
 * Controls which data are shown in Heads-Up Display (HUD).
 */
public interface HUDControlData {

    boolean isVisible();
    void show(boolean visible);

    boolean isLevelCounterVisible();
    void showLevelCounter(boolean visible);

    boolean isLivesCounterVisible();
    void showLivesCounter(boolean visible);

    boolean isScoreVisible();
    void showScore(boolean visible);

    boolean isCreditVisible();
    void showCredit(boolean visible);

    // Fluent API
    default HUDControlData all(boolean visible) {
        return creditVisible(visible)
            .scoreVisible(visible)
            .levelCounterVisible(visible)
            .livesCounterVisible(visible);
    }
    default HUDControlData creditVisible(boolean visible) {
        showCredit(visible);
        return this;
    }
    default HUDControlData levelCounterVisible(boolean visible) {
        showLevelCounter(visible);
        return this;
    }
    default HUDControlData livesCounterVisible(boolean visible) {
        showLivesCounter(visible);
        return this;
    }
    default HUDControlData scoreVisible(boolean visible) {
        showScore(visible);
        return this;
    }
}