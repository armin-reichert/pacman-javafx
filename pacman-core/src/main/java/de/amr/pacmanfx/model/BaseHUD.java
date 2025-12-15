/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

public abstract class BaseHUD implements HUD {

    private boolean visible = true;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private int     visibleLifeCount;

    @Override
    public void show() { visible = true; }

    @Override
    public void hide() {
        visible = false;
    }

    @Override
    public boolean isVisible() { return visible; }

    @Override
    public boolean isLevelCounterVisible() {
        return levelCounterVisible;
    }

    @Override
    public void showLevelCounter(boolean b) {
        levelCounterVisible = b;
    }

    @Override
    public boolean isLivesCounterVisible() {
        return livesCounterVisible;
    }

    @Override
    public void showLivesCounter(boolean b) {
        livesCounterVisible = b;
    }

    @Override
    public int visibleLifeCount() {
        return visibleLifeCount;
    }

    @Override
    public int maxLivesDisplayed() {
        return 5;
    }

    @Override
    public void setVisibleLifeCount(int count) {
        visibleLifeCount = count;
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void showScore(boolean b) {
        scoreVisible = b;
    }
}
