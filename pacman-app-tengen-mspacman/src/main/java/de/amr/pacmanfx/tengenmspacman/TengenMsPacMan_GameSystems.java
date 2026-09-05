package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;

public class TengenMsPacMan_GameSystems extends GameSystems {

    @Override
    protected void createHUDSystems() {
        scoreSystem = new ScoreSystem();
        levelCounterSystem = new LevelCounterSystem();
        hudUpdateSystem = new TengenMsPacMan_HUD_UpdateSystem();
    }
}
