package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.model.level.GameLevel;

public class PacManXXL_MsPacMan_GameRules extends ArcadeMsPacMan_GameRules {

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int totalFoodCount = level.worldMap().foodLayer().totalFoodCount();
        final int pelletsEaten = level.worldMap().foodLayer().eatenFoodCount();
        // XXL maps may have different food count, use heuristic values
        return pelletsEaten == totalFoodCount / 4 || pelletsEaten == totalFoodCount * 3 / 4;
    }
}
