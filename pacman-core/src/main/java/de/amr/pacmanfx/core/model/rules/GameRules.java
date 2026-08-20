/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.rules;

import de.amr.pacmanfx.core.level.GameLevel;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface GameRules {

    ActorCollisionRules actorCollisionRules();

    ActorSpeedRules actorSpeedRules();

    ScoringRules scoringRules();

    boolean isLevelCompleted(GameLevel level);

    int lastLevelNumber();

    int numLevelFlashes(int levelNumber);

    /**
     * @return minimum duration of demo level (in seconds) or {@code Optional#empty()} if unlimited
     */
    default Optional<Float> demoLevelMinDurationSec() {
        return Optional.empty(); // no limit
    }

    default int restingTicksForPellet() {
        return 0;
    }

    default int restingTicksForEnergizer() {
        return 0;
    }

    List<Integer> bonusSymbols(int levelNumber);

    float eatenBonusDisplaySeconds();

    /**
     * @param levelNumber level number
     * @return (optional) number (1,2,...) of cut scene to be played after this level
     */
    OptionalInt cutSceneAfterLevel(int levelNumber);

    int lastCutSceneNumber();

    // Hunting

    float pacPowerSeconds(int levelNumber);

    float pacPowerFadingSeconds(int levelNumber);

    int numHuntingPhases();

    /**
     * @param levelNumber game level number
     * @param phaseIndex index of hunting phase ({@code 0..numPhases - 1})
     * @return Duration (number of ticks) of phase.
     */
    long huntingPhaseDuration(int levelNumber, int phaseIndex);

    int demoLevelHuntingStartTick();
}
