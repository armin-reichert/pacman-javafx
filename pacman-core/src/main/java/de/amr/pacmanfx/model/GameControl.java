/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Defines the control contract for a game model driven by a {@link StateMachine}.
 *
 * <p>Each concrete game model owns a finite state machine that governs the
 * progression of gameplay states (boot, intro, level transitions, dying, etc.).
 * The controller layer should not implement game logic or state transitions
 * directly; instead, it delegates all state management to the model via this
 * interface.</p>
 *
 * <p>The state machine determines both the internal game flow and the scene
 * selection in the user interface. Implementations are expected to register
 * all required states and transitions during construction.</p>
 */
public interface GameControl {

    /**
     * Enumeration of well‑known game state identifiers.
     *
     * <p>These constants exist to avoid scattering string literals throughout
     * game‑variant‑independent code. Implementations may define additional
     * states, but these represent the canonical set used across Pac‑Man FX.</p>
     */
    enum StateName {
        BOOT,
        INTRO,
        SETTING_OPTIONS_FOR_START,
        STARTING_GAME_OR_LEVEL,
        HUNTING,
        LEVEL_COMPLETE,
        LEVEL_TRANSITION,
        EATING_GHOST,
        PACMAN_DYING,
        GAME_OVER,
        INTERMISSION
    }

    /**
     * Returns the state machine controlling this game model.
     *
     * @return the underlying {@link StateMachine} instance
     */
    StateMachine<Game> stateMachine();

    /**
     * Looks up a state by its identifier.
     *
     * @param stateName the state identifier
     * @return an {@link Optional} containing the state if it exists
     */
    default Optional<StateMachine.State<Game>> optState(String stateName) {
        requireNonNull(stateName);
        return stateMachine().optState(stateName);
    }

    /**
     * Returns the currently active state of the game.
     *
     * @return the active {@link StateMachine.State}
     */
    default StateMachine.State<Game> state() {
        return stateMachine().state();
    }

    default void enterState(StateMachine.State<Game> gameState) {
        stateMachine().enterState(gameState);
    }

    /**
     * Enters the state with the given identifier.
     *
     * <p>If the state does not exist, an error is logged and the current state
     * remains unchanged.</p>
     *
     * @param stateName the identifier of the state to enter
     */
    default void enterStateNamed(String stateName) {
        optState(stateName).ifPresentOrElse(
            state -> stateMachine().enterState(state),
            () -> Logger.error("Cannot enter state '{}'. No state with that name exists.", stateName)
        );
    }

    default void resumePreviousState() {
        stateMachine().resumePreviousState();
    }

    default void restartState(StateMachine.State<Game> gameState) {
        stateMachine().restart(gameState);
    }

    /**
     * Restarts the state with the given identifier.
     *
     * <p>If the state does not exist, an error is logged and no transition occurs.</p>
     *
     * @param stateName the identifier of the state to restart
     */
    default void restartStateNamed(String stateName) {
        optState(stateName).ifPresentOrElse(
            state -> stateMachine().restart(state),
            () -> Logger.error("Cannot restart in state '{}'. State not existing.", stateName)
        );
    }
}
