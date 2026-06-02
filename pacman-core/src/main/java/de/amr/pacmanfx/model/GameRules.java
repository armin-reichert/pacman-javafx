package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Bonus;

public interface GameRules {

    int pointsForPellet();

    int pointsForEnergizer();

    int pointsForBonus(Bonus bonus);

    boolean isExtraLifeAwarded(int oldScore, int newScore);

    // Helper

    default boolean crossedScoreLine(int oldScore, int newScore, int scoreLine) {
        return oldScore < scoreLine && newScore >= scoreLine;
    }
}
