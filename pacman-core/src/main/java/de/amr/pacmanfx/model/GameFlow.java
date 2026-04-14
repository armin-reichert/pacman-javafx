/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface GameFlow {

    /**
     * Enumeration of game state identifiers.
     *
     * <p>These constants exist to avoid scattering string literals throughout
     * game‑variant‑independent code. Implementations may define additional
     * states, but these represent the canonical set used across Pac‑Man FX.</p>
     */
    enum CanonicalGameState {
        BOOT,
        INTRO,
        PREPARING_GAME_START,
        STARTING_GAME_OR_LEVEL,
        LEVEL_PLAYING,
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
    default Optional<State<Game>> optState(String stateName) {
        requireNonNull(stateName);
        return stateMachine().optState(stateName);
    }

    /**
     * Returns the currently active state of the game.
     *
     * @return the active {@link State}
     */
    default State<Game> state() {
        return stateMachine().state();
    }

    default void enterState(State<Game> gameState) {
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
    default void enterStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(
            state -> stateMachine().enterState(state),
            () -> Logger.error("Cannot enter state '{}'. No state with that name exists.", stateName)
        );
    }

    default void resumePreviousState() {
        stateMachine().resumePreviousState();
    }

    default void restartState(State<Game> gameState) {
        stateMachine().restart(gameState);
    }

    /**
     * Restarts the state with the given identifier.
     *
     * <p>If the state does not exist, an error is logged and no transition occurs.</p>
     *
     * @param stateName the identifier of the state to restart
     */
    default void restartStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(
            state -> stateMachine().restart(state),
            () -> Logger.error("Cannot restart in state '{}'. State not existing.", stateName)
        );
    }
}
