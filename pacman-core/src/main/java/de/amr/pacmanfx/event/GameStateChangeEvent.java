/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameModel;

public class GameStateChangeEvent extends GameEvent {

    private final GameState oldState;
    private final GameState newState;

    @Override
    public String toString() {
        return "GameStateChangeEvent{oldState=%s, newState=%s, created=%s, game=%s".formatted(
            oldState, newState, creationTime, game
        );
    }

    public GameStateChangeEvent(GameModel game, GameState oldState, GameState newState) {
        super(game, GameEventType.GAME_STATE_CHANGED);
        this.oldState = oldState;
        this.newState = newState;
    }

    public GameState oldState() {
        return oldState;
    }

    public GameState newState() {
        return newState;
    }
}