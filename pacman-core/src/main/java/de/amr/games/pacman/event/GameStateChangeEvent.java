package de.amr.games.pacman.event;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModels;

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

    public GameStateChangeEvent(GameModels game, GameState oldState, GameState newState) {
        super(GameEventType.GAME_STATE_CHANGED, game);
        this.oldState = oldState;
        this.newState = newState;
    }
}