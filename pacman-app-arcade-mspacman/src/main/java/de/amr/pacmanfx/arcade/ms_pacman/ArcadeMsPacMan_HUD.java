package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_LevelCounter;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.LivesCounter;

public class ArcadeMsPacMan_HUD implements HUD {
    private final LivesCounter livesCounter = new LivesCounter();
    private final ArcadePacMan_LevelCounter levelCounter = new ArcadePacMan_LevelCounter();

    @Override
    public ArcadePacMan_LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public LivesCounter livesCounter() {
        return livesCounter;
    }
}
