/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

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

    StateMachine<Game> stateMachine();

    default Optional<FsmState<Game>> optState(String stateName) {
        return stateMachine().optState(stateName);
    }

    default FsmState<Game> state() {
        return stateMachine().state();
    }

    default void changeState(FsmState<Game> gameState) {
        stateMachine().changeState(gameState);
    }

    default void changeState(String stateID) {
        Optional<FsmState<Game>> optState = stateMachine().optState(stateID);
        optState.ifPresentOrElse(state -> stateMachine().changeState(state),
            () -> Logger.error("Cannot change state to '{}'. State not existing.", stateID));
    }

    default void restart(FsmState<Game> gameState) {
        stateMachine().restart(gameState);
    }

    default void resumePreviousState() {
        stateMachine().resumePreviousState();
    }

    default void restart(String stateID) {
        Optional<FsmState<Game>> optState = stateMachine().optState(stateID);
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