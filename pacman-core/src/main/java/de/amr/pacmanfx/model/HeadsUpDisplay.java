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

    public <HUD extends HeadsUpDisplay> HUD all(boolean visible) {
        return score(visible).levelCounter(visible).livesCounter(visible);
    }

    public <HUD extends HeadsUpDisplay> HUD levelCounter(boolean visible) {
        levelCounterVisible = visible;
        //noinspection unchecked
        return (HUD) this;
    }

    public <HUD extends HeadsUpDisplay> HUD livesCounter(boolean visible) {
        livesCounterVisible = visible;
        //noinspection unchecked
        return (HUD) this;
    }

    public <HUD extends HeadsUpDisplay> HUD score(boolean visible) {
        scoreVisible = visible;
        //noinspection unchecked
        return (HUD) this;
    }

    public void show() { visible = true; }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() { return visible; }

    public boolean isLevelCounterVisible() {
        return levelCounterVisible;
    }

    public boolean isLivesCounterVisible() {
        return livesCounterVisible;
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
}