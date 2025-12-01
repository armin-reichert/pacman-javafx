/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.Game;

public class GameStateChangeEvent extends GameEvent {

    private final FsmState<Game> oldState;
    private final FsmState<Game> newState;

    @Override
    public String toString() {
        return "GameStateChangeEvent{oldState=%s, newState=%s, created=%s, game=%s".formatted(
            oldState, newState, creationTime, game
        );
    }

    public GameStateChangeEvent(Game game, FsmState<Game> oldState, FsmState<Game> newState) {
        super(game, Type.GAME_STATE_CHANGED);
        this.oldState = oldState;
        this.newState = newState;
    }

    public FsmState<Game> oldState() {
        return oldState;
    }

    public FsmState<Game> newState() {
        return newState;
    }
}