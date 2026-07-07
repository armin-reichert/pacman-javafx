package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.flow.StateMachineControlledGameFlow;

public class TengenMsPacMan_GameFlow extends StateMachineControlledGameFlow {

    public TengenMsPacMan_GameFlow() {
        super("Tengen Ms. Pac-Man Game Flow");
        for (TengenMsPacMan_GameState gameState : TengenMsPacMan_GameState.values()) {
            addState(gameState.state());
        }
        stateMachine.addStateChangeListener((oldState, newState) ->
            context().eventManager().publishGameEvent(new GameStateChangeEvent(oldState, newState)));
    }
}
