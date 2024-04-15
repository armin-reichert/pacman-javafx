/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * Interface implemented by all states (enums) of a FSM. Each state has a timer.
 *
 * @param <C> the (context) type that the hook methods {@link #onEnter(C)}, {@link #onUpdate(C)}, {@link #onExit(C)} get
 *            passed as parameter
 * @author Armin Reichert
 */
public interface FsmState<C> {

    /**
     * The hook method that gets executed when the state is entered.
     *
     * @param context the "context" (data type provided to the state)
     */
    default void onEnter(C context) {
    }

    /**
     * The hook method that gets executed when the state is updated.
     *
     * @param context the "context" (data type provided to the state)
     */
    void onUpdate(C context);

    /**
     * The hook method that gets executed when the state is exited.
     *
     * @param context the "context" (data type provided to the state)
     */
    default void onExit(C context) {
    }

    /**
     * @return the timer of this state
     */
    TickTimer timer();
}