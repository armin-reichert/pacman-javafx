/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.fsm;

import de.amr.pacmanfx.lib.timer.TickTimer;
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
 * State transitions are defined dynamically via the {@link #changeState} method calls. Each state change triggers an
 * event.
 *
 * @author Armin Reichert
 *
 * @param <CONTEXT> Type of the data provided to the state lifecycle methods
 * {@link State#onEnter}, {@link State#onUpdate} and {@link State#onExit}
 */
public class StateMachine<CONTEXT> {

    /**
     * Interface implemented by all states (enums) of a FSM. Each state has a timer.
     *
     * @param <C> the (context) type that the hook methods {@link #onEnter(C)}, {@link #onUpdate(C)}, {@link #onExit(C)} get
     *            passed as parameter
     */
    public interface State<C> {

        String name();

        default boolean matches(String... names) {
            requireNonNull(names);
            if (names.length == 0) return false;
            return Stream.of(names).anyMatch(name -> name.equals(name()));
        }

        @SuppressWarnings("unchecked")
        default <E extends Enum<E>> boolean matches(E... states) {
            requireNonNull(states);
            if (states.length == 0) return false;
            return Stream.of(states).anyMatch(state -> state.name().equals(name()));
        }

        /**
         * The hook method that gets executed when the state is entered.
         *
         * @param context the "context" (data type provided to the state)
         */
        default void onEnter(C context) {}

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
        default void onExit(C context) {}

        /**
         * @return the timer of this state
         */
        TickTimer timer();
    }

    protected final List<FsmStateChangeListener<State<CONTEXT>>> stateChangeListeners = new ArrayList<>(5);
    protected CONTEXT context;
    protected Set<State<CONTEXT>> states = new HashSet<>();
    protected State<CONTEXT> currentState;
    protected State<CONTEXT> prevState;

    protected String name = getClass().getSimpleName();

    public StateMachine() {}

    public void addState(State<CONTEXT> state) {
        requireNonNull(state);
        if (states.contains(state)) {
            Logger.warn("State '{}' is already contained in set of states of FSM '{}'", state.name(), name);
        } else {
            states.add(state);
        }
    }

    public void addStates(Collection<State<CONTEXT>> states) {
        requireNonNull(states);
        if (states.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one state in a FSM");
        }
        for (State<CONTEXT> state : states) {
            addState(state);
        }
    }

    public void addStates(State<CONTEXT>[] states) {
        addStates(List.of(states));
    }

    public void setName(String name) {
        this.name = requireNonNull(name);
    }

    public CONTEXT context() {
        return context;
    }

    public void setContext(CONTEXT context) {
        this.context = requireNonNull(context);
    }

    @Override
    public String toString() {
        return String.format("StateMachine[name=%s, state=%s, prevState=%s]", name, currentState, prevState);
    }

    /**
     * @return the current state
     */
    public State<CONTEXT> state() {
        return currentState;
    }

    /**
     * @return (Unmodifiable) list of the state objects
     */
    public Set<State<CONTEXT>> states() {
        return Collections.unmodifiableSet(states);
    }

    /**
     * @param name state name (id)
     * @return (optional) state with given name.
     */
    public Optional<State<CONTEXT>> optState(String name) {
        requireNonNull(name);
        return states().stream()
            .filter(s -> s.name().equals(name))
            .findFirst();
    }

    /**
     * @return the previous state (can be null)
     */
    public State<CONTEXT> prevState() {
        return prevState;
    }

    /**
     * Adds a state change listener (if not yet added).
     *
     * @param listener a state change listener
     */
    public void addStateChangeListener(FsmStateChangeListener<State<CONTEXT>> listener) {
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
    public void removeStateChangeListener(FsmStateChangeListener<State<CONTEXT>> listener) {
        requireNonNull(listener);
        stateChangeListeners.remove(listener);
    }

    /**
     * Resets the timer of each state to {@link TickTimer#INDEFINITE}.
     */
    public void resetTimers() {
        for (State<CONTEXT> state : states) {
            state.timer().resetIndefiniteTime();
        }
    }

    /**
     * Sets the state machine to the given state. All timers are reset.
     * The state's entry hook method is executed but the current state's exit method isn't.
     *
     * @param state the state to enter
     */
    public void restart(State<CONTEXT> state) {
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
    public void changeState(State<CONTEXT> newState) {
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
     * Runs the {@link State#onUpdate} hook method (if defined) of the current state and advances the state timer.
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