package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;

public record GameStateChangeEvent(StateMachine.State<Game> oldState, StateMachine.State<Game> newState)
    implements GameEventNG {}
