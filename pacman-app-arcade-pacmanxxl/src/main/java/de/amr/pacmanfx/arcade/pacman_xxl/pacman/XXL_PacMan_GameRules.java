/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.level.GameLevel;

public class XXL_PacMan_GameRules extends ArcadePacMan_GameRules {

    public XXL_PacMan_GameRules() {
        scoringRules = new ArcadePacMan_ScoringRules() {
            @Override
            public boolean isBonusAwarded(GameLevel level) {
                final int total = level.food().totalFoodCount();
                final int eaten = level.food().eatenFoodCount();
                // XXL maps may have different food count, use heuristic values
                return eaten == total / 4 || eaten == total * 3 / 4;
            }
        };
    }
}
