package de.amr.pacmanfx.arcade.ms_pacman.systems;

import de.amr.pacmanfx.arcade.pacman.systems.ArcadePacMan_GameSystems;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;

public class ArcadeMsPacMan_GameSystems extends ArcadePacMan_GameSystems {

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
