/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;

import de.amr.pacmanfx.core.GameContext;

import java.util.Optional;

public interface HuntingTimer {

    void reset();

    void update(GameRules rules, int levelNumber);

    boolean inChasingPhase();

    boolean inScatteringPhase();

    void stop();

    void start();

    void startFirstPhase(GameContext game, int number);

    int phaseIndex();

    HuntingPhase currentHuntingPhase();

    long tickCount();

    long remainingTicksOfCurrentPhase();

    Optional<Integer> currentChasingPhaseIndex();

    Optional<Integer> currentScatterPhaseIndex();

    boolean isStopped();

    long durationTicks();
}
