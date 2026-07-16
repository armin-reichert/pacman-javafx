/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.flow;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameStateChangeEvent;
import de.amr.pacmanfx.core.state.GameState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * A game flow implementation using a state machine.
 */
public class GameFlowController extends StateMachine<GameContext> {

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    protected GameContext gameContext;

    public GameFlowController(String name) {
        setName(name);
        addStateChangeListener((oldState, newState) -> {
            if (gameContext != null) {
                gameContext.eventManager().publishGameEvent(new GameStateChangeEvent(gameContext, oldState, newState));
            } else {
                Logger.error("No game context available when game event is fired");
            }
        });
    }

    @Override
    public GameState state() {
        return (GameState) super.state();
    }

    public void setGameContext(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public void enterState(GameContext gameContext, Identifier id) {
        Objects.requireNonNull(id);
        enterStateWithName(gameContext, id.name());
    }

    public void restartState(GameContext gameContext, Identifier stateID) {
        restartState(gameContext, stateID.name());
    }

    public Optional<State<GameContext>> optState(Identifier stateID) {
        return super.optState(stateID.name());
    }

    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }
}
