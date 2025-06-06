package de.amr.pacmanfx.event;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameModel;

/**
 * @author Armin Reichert
 */
public class GameStateChangeEvent extends GameEvent {

    public final GameState oldState;
    public final GameState newState;

    @Override
    public String toString() {
        return "GameStateChangeEvent{" +
            "oldState=" + oldState +
            ", newState=" + newState +
            ", game=" + game +
            '}';
    }

    public GameStateChangeEvent(GameModel game, GameState oldState, GameState newState) {
        super(game, GameEventType.GAME_STATE_CHANGED);
        this.oldState = oldState;
        this.newState = newState;
    }
}