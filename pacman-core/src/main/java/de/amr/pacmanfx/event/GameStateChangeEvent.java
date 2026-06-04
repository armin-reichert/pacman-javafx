/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;

public record GameStateChangeEvent(
    GameContext context,
    State<GameContext> oldState,
    State<GameContext> newState) implements GameEvent {}
