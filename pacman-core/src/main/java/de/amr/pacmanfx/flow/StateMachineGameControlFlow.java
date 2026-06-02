/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.model.GameModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class StateMachineGameControlFlow implements GameControlFlow {

    protected final StateMachine<GameModel> stateMachine = new StateMachine<>();
    private final Set<GameEventListener> eventListeners = new HashSet<>();
    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);

    public StateMachineGameControlFlow(String name, GameModel gameModel) {
        requireNonNull(gameModel);
        stateMachine.setName(name);
        stateMachine.setContext(gameModel);
        stateMachine.addStateChangeListener((oldState, newState) -> publishGameEvent(new GameStateChangeEvent(gameModel, oldState, newState)));
    }

    @Override
    public State<GameModel> state() {
        return stateMachine.state();
    }

    @Override
    public Optional<State<GameModel>> optState(String stateName) {
        return stateMachine.optState(stateName);
    }

    @Override
    public void addState(State<GameModel> gameState) {
        stateMachine.addState(gameState);
    }

    @Override
    public void enterState(State<GameModel> gameState) {
        stateMachine.enterState(gameState);
    }

    @Override
    public void enterStateWithName(String stateName) {
        stateMachine.enterStateWithName(stateName);
    }

    @Override
    public void resumePreviousState() {
        stateMachine.resumePreviousState();
    }

    @Override
    public void restartState(State<GameModel> gameState) {
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

    /**
     * Registers a {@link GameEventListener}.
     *
     * @param listener the listener to add
     */
    @Override
    public void addGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        final boolean added = eventListeners.add(listener);
        if (added) {
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    /**
     * Removes a previously registered {@link GameEventListener}.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    /**
     * Publishes a {@link GameEvent} to all registered listeners.
     *
     * @param event the event to publish
     */
    @Override
    public void publishGameEvent(GameEvent event) {
        requireNonNull(event);
        if (Logger.isTraceEnabled()) {
            Logger.trace("Publish game event: {}", event);
        }
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
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
