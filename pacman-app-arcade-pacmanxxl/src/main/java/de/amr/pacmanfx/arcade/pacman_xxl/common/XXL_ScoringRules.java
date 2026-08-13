/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_ScoringRules;
import de.amr.pacmanfx.core.level.GameLevel;

public class XXL_ScoringRules extends ArcadeMsPacMan_ScoringRules {

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int total = level.food().totalFoodCount();
        final int eaten = level.food().eatenFoodCount();
        // XXL maps may have different food count, use heuristic values
        return eaten == total / 4 || eaten == total * 3 / 4;
    }
}
