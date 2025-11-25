/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

public class GameStateMachine extends StateMachine<FsmState<GameContext>, GameContext> {

    public GameStateMachine(Game game) {
        setContext(THE_GAME_BOX);
        addStateChangeListener((oldState, newState) -> game.publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public FsmState<GameContext> stateByName(String name) {
        return states().stream()
            .filter(state -> state.name().equals(name))
            .findFirst().orElseThrow();
    }
}
