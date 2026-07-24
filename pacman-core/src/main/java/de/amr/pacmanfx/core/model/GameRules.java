/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.actors.ActorSpeedRules;
import de.amr.pacmanfx.core.model.actors.CollisionStrategy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

import java.util.OptionalInt;

public interface GameRules {

    ActorSpeedRules actorSpeedControl();

    ScoringRules scoringRules();

    ObjectProperty<CollisionStrategy> collisionStrategyProperty();

    default CollisionStrategy getCollisionStrategy() {
        return collisionStrategyProperty().get();
    }

    BooleanProperty collisionDoubleCheckedProperty();

    boolean isLevelCompleted(GameLevel level);

    int lastLevelNumber();

    default int restingTicksForPellet() {
        return 0;
    }

    default int restingTicksForEnergizer() {
        return 0;
    }

    int selectBonusSymbolCode(int levelNumber, int bonusIndex);

    float eatenBonusDisplaySeconds();

    /**
     * @param levelNumber level number
     * @return (optional) number (1,2,...) of cut scene to be played after this level
     */
    OptionalInt cutSceneAfterLevel(int levelNumber);

    int lastCutSceneNumber();

    // Hunting

    int numHuntingPhases();

    /**
     * @param levelNumber game level number
     * @param phaseIndex index of hunting phase ({@code 0..numPhases - 1})
     * @return Duration (number of ticks) of phase.
     */
    long huntingPhaseDuration(int levelNumber, int phaseIndex);
}
