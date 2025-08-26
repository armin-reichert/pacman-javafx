package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.HUDData;

public class TengenMsPacMan_HUDData implements HUDData {
    private final TengenMsPacMan_LevelCounter levelCounter = new TengenMsPacMan_LevelCounter();

    private boolean visible = true;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private boolean gameOptionsVisible = false;

    public void showGameOptions(boolean b) {
        gameOptionsVisible = b;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }

    @Override
    public void show(boolean b) { visible = b; }

    @Override
    public boolean isVisible() { return visible; }

    @Override
    public boolean isCreditVisible() { return false; }

    @Override
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
}
