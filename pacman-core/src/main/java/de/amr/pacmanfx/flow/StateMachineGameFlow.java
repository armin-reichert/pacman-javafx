/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Optional;

public class StateMachineGameFlow implements GameFlow {

    protected final StateMachine<GameContext> stateMachine = new StateMachine<>();
    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    public StateMachineGameFlow(String name) {
        stateMachine.setName(name);
    }

    @Override
    public void setGameContext(GameContext gameContext) {
        stateMachine.setContext(gameContext);
    }

    @Override
    public GameContext gameContext() {
        return stateMachine.context();
    }

    @Override
    public GameState state() {
        return (GameState) stateMachine.state();
    }

    @Override
    public Optional<State<GameContext>> optState(Identifier stateID) {
        return stateMachine.optState(stateID.name());
    }

    @Override
    public void addState(State<GameContext> gameState) {
        stateMachine.addState(gameState);
    }

    @Override
    public void enterState(State<GameContext> gameState) {
        stateMachine.enterState(gameState);
    }

    @Override
    public void enterState(String stateName) {
        stateMachine.enterStateWithName(stateName);
    }

    @Override
    public void resumePreviousState() {
        stateMachine.resumePreviousState();
    }

    @Override
    public void restartState(State<GameContext> gameState) {
        stateMachine.restartState(gameState);
    }

    @Override
    public void restartState(String stateName) {
        stateMachine.restartState(stateName);
    }

    @Override
    public void makeStep() {
        stateMachine.update();
    }

    // Cut scenes

    @Override
    public boolean cutScenesEnabled() {
        return cutScenesEnabled.get();
    }

    @Override
    public void setCutScenesEnabled(boolean enabled) {
        cutScenesEnabled.set(enabled);
    }
}
