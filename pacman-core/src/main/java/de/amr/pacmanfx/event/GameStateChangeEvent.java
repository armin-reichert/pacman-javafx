/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;

public record GameStateChangeEvent(StateMachine.State<Game> oldState, StateMachine.State<Game> newState)
    implements GameEvent {}
