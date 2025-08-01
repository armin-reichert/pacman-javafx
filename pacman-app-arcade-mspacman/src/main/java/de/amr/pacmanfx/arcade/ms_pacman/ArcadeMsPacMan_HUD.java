package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_LevelCounter;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.LivesCounter;

public class ArcadeMsPacMan_HUD implements HUD {

    private final LivesCounter livesCounter = new LivesCounter();
    private final ArcadePacMan_LevelCounter levelCounter = new ArcadePacMan_LevelCounter();

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

    @Override
    public ArcadePacMan_LevelCounter theLevelCounter() {
        return levelCounter;
    }

    @Override
    public LivesCounter theLivesCounter() {
        return livesCounter;
    }
}
