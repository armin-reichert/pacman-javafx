package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost.ArcadeMsPacMan_ShadowHuntingStrategy;
import de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost.ArcadeMsPacMan_SpeedyHuntingStrategy;
import de.amr.pacmanfx.core.model.systems.common.DefaultGameSystems;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;

public class ArcadeMsPacMan_GameSystems extends DefaultGameSystems  {

    public ArcadeMsPacMan_GameSystems() {
    }

    @Override
    protected GhostHuntingStrategy createShadowHuntingStrategy() {
        return new ArcadeMsPacMan_ShadowHuntingStrategy(navigator);
    }

    @Override
    protected GhostHuntingStrategy createSpeedyHuntingStrategy() {
        return new ArcadeMsPacMan_SpeedyHuntingStrategy(navigator);
    }
}
