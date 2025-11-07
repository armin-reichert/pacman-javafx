/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.model.Game;

public class GameStateChangeEvent extends GameEvent {

    private final FsmState<GameContext> oldState;
    private final FsmState<GameContext> newState;

    @Override
    public String toString() {
        return "GameStateChangeEvent{oldState=%s, newState=%s, created=%s, game=%s".formatted(
            oldState, newState, creationTime, game
        );
    }

    public GameStateChangeEvent(Game game, FsmState<GameContext> oldState, FsmState<GameContext> newState) {
        super(game, GameEventType.GAME_STATE_CHANGED);
        this.oldState = oldState;
        this.newState = newState;
    }

    public FsmState<GameContext> oldState() {
        return oldState;
    }

    public FsmState<GameContext> newState() {
        return newState;
    }
}