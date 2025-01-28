/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.fsm;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.lib.timer.TickTimer.State;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A finite-state machine.
 * <p>
 * The states must be provided by an enumeration type that implements the {@link FsmState} interface. The data type
 * passed to the state lifecycle methods is specified by the second type parameter.
 * <p>
 * State transitions are defined dynamically via the {@link #changeState} method calls. Each state change triggers an
 * event.
 *
 * @author Armin Reichert
 *
 * @param <S> "State". Enumeration type providing the states of this FSM.
 * @param <C> "Context". Type of the data provided to the state lifecycle methods {@link FsmState#onEnter},
 *            {@link FsmState#onUpdate} and {@link FsmState#onExit}
 */
public abstract class FiniteStateMachine<S extends FsmState<C>, C> {

    protected final List<FsmStateChangeListener<S>> stateChangeListeners = new ArrayList<>(5);
    protected final S[] states;
    protected S currentState;
    protected S prevState;
    protected String name = getClass().getSimpleName();

    protected FiniteStateMachine(S[] states) {
        this.states = states;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("FSM[name=%s, state=%s, prev=%s]", name, currentState, prevState);
    }

    /**
     * @return the context passed to the state lifecycle methods
     */
    public abstract C context();

    /**
     * @return the current state
     */
    public S state() {
        return currentState;
    }

    /**
     * @return the previous state (can be null)
     */
    public S prevState() {
        return prevState;
    }

    /**
     * Adds a state change listener.
     *
     * @param listener a state change listener
     */
    public void addStateChangeListener(FsmStateChangeListener<S> listener) {
        stateChangeListeners.add(listener);
    }

    /**
     * Removes a state change listener.
     *
     * @param listener a state change listener
     */
    public void removeStateChangeListener(FsmStateChangeListener<S> listener) {
        stateChangeListeners.remove(listener);
    }

    /**
     * Resets the timer of each state to {@link TickTimer#INDEFINITE}.
     */
    public void resetTimers() {
        for (S state : states) {
            state.timer().resetIndefiniteTime();
        }
    }

    /**
     * Sets the state machine to the given state. All timers are reset.
     * The state's entry hook method is executed but the current state's exit method isn't.
     *
     * @param state the state to enter
     */
    public void restart(S state) {
        resetTimers();
        currentState = null;
        changeState(state);
    }

    /**
     * Lets the timer of the current game state expire.
     */
    public void terminateCurrentState() {
        state().timer().expire();
    }

    /**
     * Changes the machine's current state to the new state. Tne exit hook method of the current state is executed before
     * entering the new state. The new state's entry hook method is executed and its timer is reset to
     * {@link TickTimer#INDEFINITE}. After the state change, an event is published.
     * <p>
     * Trying to change to the current state (self loop) leads to a runtime exception.
     *
     * @param newState the new state
     */
    public void changeState(S newState) {
        if (newState == currentState) {
            throw new IllegalStateException("FiniteStateMachine: Self loop in state " + currentState);
        }
        C context = context();
        if (currentState != null) {
            currentState.onExit(context);
            Logger.debug("Exit  state {} timer={}", currentState, currentState.timer());
        }
        prevState = currentState;
        currentState = newState;
        currentState.timer().resetIndefiniteTime();
        Logger.debug("Enter state {} timer={}", currentState, currentState.timer());
        currentState.onEnter(context);
        Logger.debug("After Enter state {} timer={}", currentState, currentState.timer());
        stateChangeListeners.forEach(listener -> listener.onStateChange(prevState, currentState));
    }

    /**
     * Returns to the previous state.
     */
    public void resumePreviousState() {
        if (prevState == null) {
            throw new IllegalStateException("State machine cannot resume previous state because there is none");
        }
        Logger.debug("Resume state {}, timer= {}", prevState, prevState.timer());
        state().onExit(context());
        currentState = prevState;
    }

    /**
     * Updates this FSM's current state.
     * <p>
     * Runs the {@link FsmState#onUpdate} hook method (if defined) of the current state and advances the state timer.
     */
    public void update() {
        currentState.onUpdate(context());
        if (currentState.timer().state() == State.READY) {
            currentState.timer().start();
        } else {
            currentState.timer().doTick();
        }
    }
}