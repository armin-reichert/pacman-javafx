package de.amr.pacmanfx.model;

public class DefaultHUDControlData implements HUDControlData {

    private boolean visible = true;
    private boolean creditVisible;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private int visibleLifeCount;

    @Override
    public void show(boolean b) { visible = b; }

    @Override
    public boolean isVisible() { return visible; }

    @Override
    public boolean isCreditVisible() { return creditVisible; }

    public void showCredit(boolean b) { creditVisible = b; }

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
