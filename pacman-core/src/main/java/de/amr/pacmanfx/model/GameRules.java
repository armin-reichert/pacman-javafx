package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Bonus;

import java.util.OptionalInt;

public interface GameRules {

    int pointsForPellet();

    int pointsForEnergizer();

    int pointsForBonus(Bonus bonus);

    float eatenBonusDisplaySeconds();

    boolean isExtraLifeAwarded(int oldScore, int newScore);

    OptionalInt cutSceneNumberAfterLevel(int levelNumber);

    // Helper

    default boolean crossedScoreLine(int oldScore, int newScore, int scoreLine) {
        return oldScore < scoreLine && newScore >= scoreLine;
    }
}
