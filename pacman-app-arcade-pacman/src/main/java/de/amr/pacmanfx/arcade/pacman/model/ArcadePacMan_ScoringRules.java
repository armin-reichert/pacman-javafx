/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.core.model.ScoringRules;
import de.amr.pacmanfx.core.model.level.GameLevel;

public class ArcadePacMan_ScoringRules implements ScoringRules {

    private static final byte[] GHOST_POINT_FACTORS = { 2, 4, 8, 16 };

    @Override
    public int pointsForGhost(int killedBefore) {
        if (killedBefore < 0 || killedBefore > GHOST_POINT_FACTORS.length) {
            throw new IllegalArgumentException("killedBefore index is out of range: " + killedBefore);
        }
        return GHOST_POINT_FACTORS[killedBefore] * 100;
    }

    @Override
    public int pointsForPellet() {
        return 10;
    }

    @Override
    public int pointsForEnergizer() {
        return 50;
    }

    @Override
    public int pointsForBonus(int symbolCode) {
        return switch (symbolCode) {
            case 0 -> 100;  // cherries
            case 1 -> 300;  // strawberry
            case 2 -> 500;  // peach
            case 3 -> 700;  // apple
            case 4 -> 1000; // grapes
            case 5 -> 2000; // galaxian
            case 6 -> 3000; // bell
            case 7 -> 5000; // key
            default -> throw new IllegalArgumentException("Invalid symbol code: " + symbolCode);
        };
    }

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int pelletsEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletsEaten == 70 || pelletsEaten == 170;
    }

    @Override
    public boolean isExtraLifeAwarded(int oldScore, int newScore) {
        return ScoringRules.crossedScoreLine(oldScore, newScore, 10_000);
    }
}
