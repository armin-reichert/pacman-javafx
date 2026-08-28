/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;

import de.amr.pacmanfx.core.entities.Ghost;
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

    LevelContinuationRules levelContinuation();

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

    OptionalInt cutSceneAfterLevel(int levelNumber);

    int lastCutSceneNumber();

    float pacPowerSeconds(int levelNumber);

    float pacPowerFadingSeconds(int levelNumber);

    PacDyingTiming pacDyingTiming();

    int numHuntingPhases();

    long huntingPhaseDuration(int levelNumber, int phaseIndex);

    int demoLevelHuntingStartTick();

    float eatenGhostDisplaySeconds();

    default boolean ghostBecomesElroy1(GameLevel level, Ghost ghost) {
        return false;
    }

    default boolean ghostBecomesElroy2(GameLevel level, Ghost ghost) {
        return false;
    }
}
