/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.HuntingPhase;

public record HuntingPhaseStartedEvent(Game game, int phaseIndex, HuntingPhase phase) implements GameEvent {}
