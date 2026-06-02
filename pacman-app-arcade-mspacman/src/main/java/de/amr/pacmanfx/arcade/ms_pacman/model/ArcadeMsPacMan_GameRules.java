package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.model.GameLevel;

public class ArcadeMsPacMan_GameRules extends ArcadePacMan_GameRules {

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int pelletEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletEaten == 64 || pelletEaten == 176;
    }
}
