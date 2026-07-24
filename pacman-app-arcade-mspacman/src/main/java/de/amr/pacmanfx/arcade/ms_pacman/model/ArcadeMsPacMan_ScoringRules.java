/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;


import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;
import de.amr.pacmanfx.core.model.level.GameLevel;

public class ArcadeMsPacMan_ScoringRules extends ArcadePacMan_ScoringRules {

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int pelletEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletEaten == 64 || pelletEaten == 176;
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
}
