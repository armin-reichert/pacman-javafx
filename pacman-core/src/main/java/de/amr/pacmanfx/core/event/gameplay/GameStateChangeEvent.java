/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.gameplay;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEvent;

public record GameStateChangeEvent(State<GameContext> oldState, State<GameContext> newState) implements GameEvent {}
