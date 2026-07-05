/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

import java.util.OptionalInt;

public interface GameRules {

    ActorSpeedControl actorSpeedControl();

    ObjectProperty<CollisionStrategy> collisionStrategyProperty();

    default CollisionStrategy getCollisionStrategy() {
        return collisionStrategyProperty().get();
    }

    BooleanProperty collisionDoubleCheckedProperty();

    boolean isLevelCompleted(GameLevel level);

    int lastLevelNumber();

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

    // Hunting

    int numHuntingPhases();

    /**
     * @param levelNumber game level number
     * @param phaseIndex index of hunting phase ({@code 0..numPhases - 1})
     * @return Duration (number of ticks) of phase.
     */
    long huntingPhaseDuration(int levelNumber, int phaseIndex);

    // Helper

    default boolean crossedScoreLine(int oldScore, int newScore, int scoreLine) {
        return oldScore < scoreLine && newScore >= scoreLine;
    }
}
