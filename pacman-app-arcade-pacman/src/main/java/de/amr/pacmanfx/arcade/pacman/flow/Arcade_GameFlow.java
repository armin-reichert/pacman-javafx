/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.flow.StateMachineControlledGameFlow;

/**
 * State machine with all Arcade game states.
 */
public class Arcade_GameFlow extends StateMachineControlledGameFlow {

    public Arcade_GameFlow() {
        super("Arcade Pac-Man Games Control Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
        stateMachine.addStateChangeListener((oldState, newState) ->
            context().eventManager().publishGameEvent(new GameStateChangeEvent(oldState, newState)));
    }
}
