/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;


import de.amr.pacmanfx.core.model.level.GameLevel;

public interface ScoringRules {

    int pointsForGhost(int killedBefore);

    int pointsForPellet();

    int pointsForEnergizer();

    int pointsForBonus(int symbolCode);

    boolean isExtraLifeAwarded(int oldScore, int newScore);

    boolean isBonusAwarded(GameLevel level);

    // Helper

    static boolean crossedScoreLine(int oldScore, int newScore, int scoreLine) {
        return oldScore < scoreLine && newScore >= scoreLine;
    }
}
