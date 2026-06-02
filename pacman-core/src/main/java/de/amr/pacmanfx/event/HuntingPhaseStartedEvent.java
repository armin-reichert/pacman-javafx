/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.HuntingPhase;

public record HuntingPhaseStartedEvent(GameModel game, int phaseIndex, HuntingPhase phase) implements GameEvent {}
