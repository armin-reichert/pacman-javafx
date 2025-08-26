package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.HUDData;

public class ArcadeMsPacMan_HUDData implements HUDData {

    private boolean visible = true;
    private boolean creditVisible;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;

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
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void showScore(boolean b) {
        scoreVisible = b;
    }
}
