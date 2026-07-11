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

import java.util.Optional;

/**
 * A game flow implementation using a state machine.
 */
public class StateMachineControlledGameFlow extends StateMachine<GameContext> implements GameFlow {

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    public StateMachineControlledGameFlow(String name) {
        setName(name);
    }

    @Override
    public GameState state() {
        return (GameState) super.state();
    }

    @Override
    public Optional<State<GameContext>> optState(Identifier stateID) {
        return super.optState(stateID.name());
    }

    @Override
    public void makeStep() {
        super.update();
    }

    @Override
    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    @Override
    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }
}
