/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.test.CutScenesTestState;
import de.amr.pacmanfx.core.model.test.LevelMediumTestState;
import de.amr.pacmanfx.core.model.test.LevelShortTestState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A game flow implementation using a state machine.
 */
public class GameFlowController extends StateMachine<GameContext> {

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    public GameFlowController(String name) {
        setName(name);
    }

    public void addTestStates() {
        addState(new LevelShortTestState());
        addState(new LevelMediumTestState());
        addState(new CutScenesTestState());
    }

    @Override
    public GameState state() {
        return (GameState) super.state();
    }

    public void enterState(GameContext game, Named id) {
        requireNonNull(id);
        enterStateWithName(game, id.name());
    }

    public void restartState(GameContext game, Named stateID) {
        restartState(game, stateID.name());
    }

    public Optional<State<GameContext>> optState(Named stateID) {
        return super.optState(stateID.name());
    }

    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }
}
