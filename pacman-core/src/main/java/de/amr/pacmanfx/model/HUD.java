package de.amr.pacmanfx.model;

public interface HUD {

    LevelCounter levelCounter();
    LivesCounter livesCounter();
    // score ...

    boolean isLevelCounterVisible();
    void showLevelCounter();
    void hideLevelCounter();

    boolean isLivesCounterVisible();
    void showLivesCounter();
    void hideLivesCounter();
}
