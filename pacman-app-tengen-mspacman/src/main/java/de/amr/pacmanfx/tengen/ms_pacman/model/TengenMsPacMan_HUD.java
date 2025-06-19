package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.LivesCounter;

public class TengenMsPacMan_HUD implements HUD {
    private final TengenMsPacMan_LevelCounter levelCounter = new TengenMsPacMan_LevelCounter();
    private final LivesCounter livesCounter = new LivesCounter();

    private boolean visible = true;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;

    @Override
    public void show() {
        visible = true;
    }

    @Override
    public void hide() {
        visible = false;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean isLevelCounterVisible() {
        return levelCounterVisible;
    }

    @Override
    public void showLevelCounter() {
        levelCounterVisible = true;
    }

    @Override
    public void hideLevelCounter() {
        levelCounterVisible = false;
    }

    @Override
    public boolean isLivesCounterVisible() {
        return livesCounterVisible;
    }

    @Override
    public void showLivesCounter() {
        livesCounterVisible = true;
    }

    @Override
    public void hideLivesCounter() {
        livesCounterVisible = false;
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void showScore() {
        scoreVisible = true;
    }

    @Override
    public void hideScore() {
        scoreVisible = false;
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
