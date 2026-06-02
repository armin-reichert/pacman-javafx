/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.model.GameModel;

public record GameStateChangeEvent(GameModel game, State<GameModel> oldState, State<GameModel> newState) implements GameEvent {}
