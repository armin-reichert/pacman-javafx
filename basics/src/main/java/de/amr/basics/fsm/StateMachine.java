/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.fsm;

import de.amr.basics.timer.TickTimer;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A finite-state machine.
 * <p>
 * The states must be provided by an enumeration type that implements the {@link State} interface. The data type
 * passed to the state lifecycle methods is specified by the second type parameter.
 * <p>
 * State transitions are defined dynamically via the {@link #enterState} method calls. Each state change triggers an
 * event.
 *
 * @param <C> Type of the data provided to the state lifecycle methods ("context")
 */
public class StateMachine<C> {

    protected final Set<State<C>> states = new HashSet<>();
    protected final Set<StateChangeListener<State<C>>> stateChangeListeners = new HashSet<>(5);
    protected C context;
    protected String name = getClass().getSimpleName();
    protected State<C> state;
    protected State<C> previousState;

    public StateMachine() {}

    public StateMachine(C context, Collection<State<C>> states) {
        requireNonNull(context);
        requireNonNull(states);
        if (states.isEmpty()) {
            Logger.warn("Empty state set?");
        }
        setContext(context);
        addStates(states);
    }

    public void addState(State<C> state) {
        requireNonNull(state);
        final boolean added = states.add(state);
        if (!added) {
            Logger.warn("State '{}' is already contained in states of machine '{}'", state.name(), name);
        }
    }

    public void addStates(Collection<State<C>> states) {
        requireNonNull(states);
        if (states.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one state in a FSM");
        }
        for (State<C> state : states) {
            addState(state);
        }
    }

    public void addStates(State<C>[] states) {
        addStates(List.of(states));
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
        return String.format("StateMachine[name=%s, state=%s, prevState=%s]", name, state, previousState);
    }

    /**
     * @return the current state
     */
    public State<C> state() {
        return state;
    }

    /**
     * @return stream of the state objects
     */
    public Stream<State<C>> states() {
        return states.stream();
    }

    /**
     * @param name state name (id)
     * @return (optional) state with given name.
     */
    public Optional<State<C>> optState(String name) {
        requireNonNull(name);
        return states().filter(s -> s.name().equals(name)).findFirst();
    }

    /**
     * @return the previous state (can be null)
     */
    public State<C> previousState() {
        return previousState;
    }

    /**
     * Adds a state change listener (if not yet added).
     *
     * @param listener a state change listener
     */
    public void addStateChangeListener(StateChangeListener<State<C>> listener) {
        requireNonNull(listener);
        final boolean added = stateChangeListeners.add(listener);
        if (!added) {
            Logger.warn("State change listener already added: {}", listener);
        }
    }

    /**
     * Removes a state change listener.
     *
     * @param listener a state change listener
     */
    public void removeStateChangeListener(StateChangeListener<State<C>> listener) {
        requireNonNull(listener);
        stateChangeListeners.remove(listener);
    }

    /**
     * Resets the timer of each state to {@link TickTimer#INDEFINITE}.
     */
    public void resetTimers() {
        for (State<C> state : states) {
            state.timer().resetToIndefiniteDuration();
        }
    }

    /**
     * Sets the state machine to the given state. All timers are reset.
     * The state's entry hook method is executed but the current state's exit method isn't.
     *
     * @param state the state to enter
     */
    public void restartState(State<C> state) {
        requireNonNull(state);
        resetTimers();
        this.state = null;
        enterState(state);
    }

    public void restartStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(this::restartState,
            () -> Logger.error("No state named {} found", stateName));
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
    public void enterState(State<C> newState) {
        requireNonNull(newState);
        if (newState == state) {
            Logger.info("State machine is already in state {}", state);
            return;
        }
        if (state != null) {
            state.onExit(context);
            Logger.debug("Exit  state {} timer={}", state, state.timer());
        }
        previousState = state;
        state = newState;
        state.timer().resetToIndefiniteDuration();
        Logger.debug("Enter state {} timer={}", state, state.timer());
        state.onEnter(context);
        Logger.debug("After Enter state {} timer={}", state, state.timer());
        stateChangeListeners.forEach(listener -> listener.onStateChange(previousState, state));
    }

    public void enterStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(this::enterState,
            () -> Logger.error("No state named {} found", stateName));
    }

    /**
     * Returns to the previous state.
     */
    public void resumePreviousState() {
        if (previousState == null) {
            throw new IllegalStateException("State machine cannot resume previous state: no previous state exists");
        }
        if (state == null) {
            throw new IllegalStateException("State machine cannot resume previous state: current state is not defined");
        }
        Logger.debug("Resume state {}, timer= {}", previousState, previousState.timer());
        state.onExit(context);
        state = previousState;
    }

    /**
     * Updates this FSM's current state.
     * <p>
     * Runs the {@link State#onUpdate} hook method (if defined) of the current state and advances the state timer.
     */
    public void update() {
        if (state == null) {
            throw new IllegalStateException("State machine cannot be updated: current state is not defined");
        }
        state.onUpdate(context);
        if (state.timer().state() == TickTimer.State.READY) {
            state.timer().start();
        } else {
            state.timer().doTick();
        }
    }
}