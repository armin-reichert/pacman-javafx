/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.HuntingPhase;

public record HuntingPhaseStartedEvent(GameContext gameContext, int phaseIndex, HuntingPhase phase) implements GameEvent {}
