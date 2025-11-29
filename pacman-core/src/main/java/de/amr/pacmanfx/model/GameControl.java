/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import org.tinylog.Logger;

import java.util.Optional;

/**
 * Each game model has a finite state machine which controls the game state transitions and eventually the scene
 * selection in the user interface. However, the game controller should not contain the details how the game works,
 * this should be implemented in the model itself.
*/
public interface GameControl {

    StateMachine<FsmState<GameContext>, GameContext> stateMachine();

    default Optional<FsmState<GameContext>> optState(String stateName) {
        return stateMachine().optState(stateName);
    }

    default FsmState<GameContext> state() {
        return stateMachine().state();
    }

    default void changeState(FsmState<GameContext> gameState) {
        stateMachine().changeState(gameState);
    }

    default void changeState(String stateID) {
        Optional<FsmState<GameContext>> optState = stateMachine().optState(stateID);
        optState.ifPresentOrElse(state -> stateMachine().changeState(state),
            () -> Logger.error("Cannot change state to '{}'. State not existing.", stateID));
    }

    default void restart(FsmState<GameContext> gameState) {
        stateMachine().restart(gameState);
    }

    default void resumePreviousState() {
        stateMachine().resumePreviousState();
    }

    default void restart(String stateID) {
        Optional<FsmState<GameContext>> optState = stateMachine().optState(stateID);
        optState.ifPresentOrElse(state -> stateMachine().restart(state),
            () -> Logger.error("Cannot restart in state to '{}'. State not existing.", stateID));
    }

    default void terminateCurrentGameState() {
        stateMachine().state().timer().expire();
    }

    default void update() {
        stateMachine().update();
    }
}