/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;

import de.amr.pacmanfx.core.GameContext;

import java.util.Optional;

public interface HuntingRules {

    void reset();

    void update(GameRules rules, int number);

    boolean isChasing();

    boolean isScattering();

    void stop();

    void start();

    void startFirstPhase(GameContext gameContext, int number);

    int phaseIndex();

    HuntingPhase currentHuntingPhase();

    long tickCount();

    long remainingTicksOfCurrentPhase();

    Optional<Integer> currentChasingPhaseIndex();

    Optional<Integer> currentScatterPhaseIndex();

    boolean isStopped();

    long durationTicks();
}
