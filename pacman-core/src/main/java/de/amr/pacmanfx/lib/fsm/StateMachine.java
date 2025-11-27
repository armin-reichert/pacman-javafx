/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.fsm;

import de.amr.pacmanfx.lib.timer.TickTimer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

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
 * @param <S> "State". Type (can be an enum) providing the states of this FSM.
 * @param <C> "Context". Type of the data provided to the state lifecycle methods {@link FsmState#onEnter},
 *            {@link FsmState#onUpdate} and {@link FsmState#onExit}
 */
public class StateMachine<S extends FsmState<C>, C> {

    protected final List<FsmStateChangeListener<S>> stateChangeListeners = new ArrayList<>(5);
    protected C context;
    protected List<S> states;
    protected S currentState;
    protected S prevState;

    protected String name = getClass().getSimpleName();

    public StateMachine() {}

    public void setStates(List<S> states) {
        requireNonNull(states);
        if (states.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one state in a FSM");
        }
        this.states = new ArrayList<>(states);
    }

    public void setName(String name) {
        this.name = requireNonNull(name);
    }

    public C context() {
        return context;
    }

    public void setContext(C context) {
        this.context = requireNonNull(context);
    }

    @Override
    public String toString() {
        return String.format("StateMachine[name=%s, state=%s, prevState=%s]", name, currentState, prevState);
    }

    /**
     * @return the current state
     */
    public S state() {
        return currentState;
    }

    /**
     * @return (Unmodifiable) list of the state objects
     */
    public List<S> states() {
        return Collections.unmodifiableList(states);
    }

    /**
     * @param name state name (id)
     * @return state with given name. If no such name exists, an exception is thrown
     */
    public S state(String name) {
        requireNonNull(name);
        return states().stream()
            .filter(state -> state.name().equals(name))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    /**
     * @return the previous state (can be null)
     */
    public S prevState() {
        return prevState;
    }

    /**
     * Adds a state change listener (if not yet added).
     *
     * @param listener a state change listener
     */
    public void addStateChangeListener(FsmStateChangeListener<S> listener) {
        requireNonNull(listener);
        if (!stateChangeListeners.contains(listener)) {
            stateChangeListeners.add(listener);
        }
        else {
            Logger.warn("Attempt to add state change listener twice: {}", listener);
        }
    }

    /**
     * Removes a state change listener.
     *
     * @param listener a state change listener
     */
    public void removeStateChangeListener(FsmStateChangeListener<S> listener) {
        requireNonNull(listener);
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
        requireNonNull(state);
        resetTimers();
        currentState = null;
        changeState(state);
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
        requireNonNull(newState);
        if (newState == currentState) {
            Logger.info("State machine is already in state {}", currentState);
            return;
        }
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
        currentState.onExit(context);
        currentState = prevState;
    }

    /**
     * Updates this FSM's current state.
     * <p>
     * Runs the {@link FsmState#onUpdate} hook method (if defined) of the current state and advances the state timer.
     */
    public void update() {
        currentState.onUpdate(context);
        if (currentState.timer().state() == TickTimer.State.READY) {
            currentState.timer().start();
        } else {
            currentState.timer().doTick();
        }
    }
}