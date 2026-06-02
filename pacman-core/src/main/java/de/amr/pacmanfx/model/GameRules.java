package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.level.GameLevel;

import java.util.OptionalInt;

public interface GameRules {

    int pointsForGhost(int killedBefore);

    int pointsForPellet();

    default int restingTicksForPellet() {
        return 0;
    }

    int pointsForEnergizer();

    default int restingTicksForEnergizer() {
        return 0;
    }

    boolean isBonusAwarded(GameLevel level);

    int selectBonusSymbolCode(int levelNumber, int bonusIndex);

    int pointsForBonus(int symbolCode);

    float eatenBonusDisplaySeconds();

    boolean isExtraLifeAwarded(int oldScore, int newScore);

    OptionalInt cutSceneNumberAfterLevel(int levelNumber);

    int lastCutSceneNumber();

    // Helper

    default boolean crossedScoreLine(int oldScore, int newScore, int scoreLine) {
        return oldScore < scoreLine && newScore >= scoreLine;
    }
}
