/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rules.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.core.level.GameLevel;

public class XXL_MsPacMan_GameRules extends ArcadeMsPacMan_GameRules {

    public XXL_MsPacMan_GameRules() {
        scoringRules = new ArcadePacMan_ScoringRules() {
            @Override
            public boolean isBonusAwarded(GameLevel level) {
                final int total = level.food().totalFoodCount();
                final int eaten = level.food().eatenFoodCount();
                // XXL maps may have different food count, use heuristic values
                return eaten == total / 4 || eaten == total * 3 / 4;
            }

            @Override
            public int pointsForBonus(int symbolCode) {
                return switch (symbolCode) {
                    case 0 -> 100;  // cherries
                    case 1 -> 200;  // strawberry
                    case 2 -> 500;  // orange
                    case 3 -> 700;  // pretzel
                    case 4 -> 1000; // apple
                    case 5 -> 2000; // pear
                    case 6 -> 5000; // banana
                    default -> throw new IllegalArgumentException("Invalid symbol code: " + symbolCode);
                };
            }
        };
    }
}
