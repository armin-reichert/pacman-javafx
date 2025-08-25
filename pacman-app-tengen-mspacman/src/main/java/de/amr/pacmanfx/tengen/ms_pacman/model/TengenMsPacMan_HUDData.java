package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.HUDData;
import de.amr.pacmanfx.model.LivesCounter;

public class TengenMsPacMan_HUDData implements HUDData {
    private final TengenMsPacMan_LevelCounter levelCounter = new TengenMsPacMan_LevelCounter();
    private final LivesCounter livesCounter = new LivesCounter();

    private boolean visible = true;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;

    @Override
    public void show(boolean b) { visible = b; }

    @Override
    public boolean isVisible() { return visible; }

    @Override
    public boolean isCreditVisible() { return false; }

    public void showCredit(boolean b) { /* not needed */ }

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
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void showScore(boolean b) {
        scoreVisible = b;
    }

    @Override
    public TengenMsPacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public LivesCounter livesCounter() {
        return livesCounter;
    }
}
