package de.amr.pacmanfx.engine;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateChangeListener;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.gameplay.GameStateChangeEvent;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * A state change event from the current game flow state machine is converted
 * into a game event and published such that UI components (views, game scenes) can handle them.
 */
public record StateChangeEventMapper(GameEventManager eventManager) implements StateChangeListener<GameContext> {

    public StateChangeEventMapper(GameEventManager eventManager) {
        this.eventManager = requireNonNull(eventManager);
    }

    @Override
    public void onStateChange(State<GameContext> oldState, State<GameContext> newState) {
        Logger.info("Game state changed from {} to {}", oldState, newState);
        eventManager.publishEvent(new GameStateChangeEvent(oldState, newState));
    }
}
