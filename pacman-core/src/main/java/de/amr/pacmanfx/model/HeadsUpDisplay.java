/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public abstract class HeadsUpDisplay {

    private boolean visible = true;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private int     visibleLifeCount;

    // Fluent API

    public HeadsUpDisplay all(boolean visible) {
        return score(visible).levelCounter(visible).livesCounter(visible);
    }

    public HeadsUpDisplay levelCounter(boolean visible) {
        showLevelCounter(visible);
        return this;
    }

    public HeadsUpDisplay livesCounter(boolean visible) {
        showLivesCounter(visible);
        return this;
    }

    public HeadsUpDisplay score(boolean visible) {
        showScore(visible);
        return this;
    }

    public void show() { visible = true; }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() { return visible; }

    public boolean isLevelCounterVisible() {
        return levelCounterVisible;
    }

    public void showLevelCounter(boolean b) {
        levelCounterVisible = b;
    }

    public boolean isLivesCounterVisible() {
        return livesCounterVisible;
    }

    public void showLivesCounter(boolean b) {
        livesCounterVisible = b;
    }

    public int visibleLifeCount() {
        return visibleLifeCount;
    }

    public int maxLivesDisplayed() {
        return 5;
    }

    public void setVisibleLifeCount(int count) {
        visibleLifeCount = count;
    }

    public boolean isScoreVisible() {
        return scoreVisible;
    }

    public void showScore(boolean b) {
        scoreVisible = b;
    }
}