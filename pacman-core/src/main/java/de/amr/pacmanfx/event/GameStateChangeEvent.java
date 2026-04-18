/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.model.Game;

public record GameStateChangeEvent(Game game, State<Game> oldState, State<Game> newState) implements GameEvent {}
