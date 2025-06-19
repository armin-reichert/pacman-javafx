package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.LivesCounter;

public class TengenMsPacMan_HUD implements HUD {
    private final TengenMsPacMan_LevelCounter levelCounter = new TengenMsPacMan_LevelCounter();
    private final LivesCounter livesCounter = new LivesCounter();

    @Override
    public TengenMsPacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public LivesCounter livesCounter() {
        return livesCounter;
    }
}
