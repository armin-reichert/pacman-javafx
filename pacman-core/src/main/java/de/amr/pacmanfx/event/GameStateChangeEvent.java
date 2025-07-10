package de.amr.pacmanfx.event;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameModel;

public class GameStateChangeEvent extends GameEvent {

    public static final String PAYLOAD_KEY_OLD_STATE = "_oldstate";
    public static final String PAYLOAD_KEY_NEW_STATE = "_newstate";

    @Override
    public String toString() {
        return "GameStateChangeEvent{oldState=%s, newState=%s, created=%s, game=%s".formatted(
            oldState(), newState(), creationTime(), game()
        );
    }

    public GameStateChangeEvent(GameModel game, GameState oldState, GameState newState) {
        super(game, GameEventType.GAME_STATE_CHANGED);
        setPayload(PAYLOAD_KEY_OLD_STATE, oldState);
        setPayload(PAYLOAD_KEY_NEW_STATE, newState);
    }

    public GameState oldState() { return payload(PAYLOAD_KEY_OLD_STATE); }

    public GameState newState() { return payload(PAYLOAD_KEY_NEW_STATE); }
}