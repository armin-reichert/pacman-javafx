package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.LivesCounter;

public class ArcadePacMan_HUD implements HUD {

    private final LivesCounter livesCounter = new LivesCounter();
    private final ArcadePacMan_LevelCounter levelCounter = new ArcadePacMan_LevelCounter();

    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;

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
    public ArcadePacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public LivesCounter livesCounter() {
        return livesCounter;
    }
}
