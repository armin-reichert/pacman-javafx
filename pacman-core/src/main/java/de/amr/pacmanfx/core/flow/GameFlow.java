/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.flow;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.GameState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * A game flow implementation using a state machine.
 */
public class GameFlow extends StateMachine<GameContext> {

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    public GameFlow(String name) {
        setName(name);
    }

    public void enterState(Identifier id) {
        Objects.requireNonNull(id);
        enterStateWithName(id.name());
    }

    public void restartState(Identifier stateID) {
        restartState(stateID.name());
    }

    @Override
    public GameState state() {
        return (GameState) super.state();
    }

    public Optional<State<GameContext>> optState(Identifier stateID) {
        return super.optState(stateID.name());
    }

    public void makeStep() {
        super.update();
    }

    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }
}
