/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_ScoringRules;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.FoodLayer;

public class XXL_ScoringRules extends ArcadeMsPacMan_ScoringRules {

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final int total = foodLayer.totalFoodCount();
        final int eaten = foodLayer.eatenFoodCount();
        // XXL maps may have different food count, use heuristic values
        return eaten == total / 4 || eaten == total * 3 / 4;
    }
}
