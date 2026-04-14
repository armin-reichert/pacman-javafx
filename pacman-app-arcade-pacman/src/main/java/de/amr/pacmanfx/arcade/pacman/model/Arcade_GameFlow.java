/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Arcade_GameFlow implements GameFlow {

    private final StateMachine<Game> stateMachine = new StateMachine<>();

    public Arcade_GameFlow(Arcade_GameModel game) {
        stateMachine.setName("Arcade Pac-Man (common) Game Control");
        stateMachine.setContext(game);
        stateMachine.addStateChangeListener((oldState, newState) -> publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
        stateMachine.addStates(Arcade_GameState.values());
    }

    @Override
    public void updateState() {
        stateMachine.update();
    }

    @Override
    public void addState(State<Game> gameState) {
        stateMachine.addState(gameState);
    }

    @Override
    public Optional<State<Game>> optState(String stateName) {
        return stateMachine.optState(stateName);
    }

    @Override
    public State<Game> state() {
        return stateMachine.state();
    }

    @Override
    public void enterState(State<Game> gameState) {
        stateMachine.enterState(gameState);
    }

    @Override
    public void enterStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(stateMachine::enterState,
            () -> Logger.error("No game state named {} found", stateName));
    }

    @Override
    public void resumePreviousState() {
        stateMachine.resumePreviousState();
    }

    @Override
    public void restartState(State<Game> gameState) {
        stateMachine.restart(gameState);
    }

    @Override
    public void restartStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(stateMachine::restart,
            () -> Logger.error("No game state named {} found", stateName));

    }

    private final Set<GameEventListener> eventListeners = new HashSet<>();

    /**
     * Registers a {@link GameEventListener}.
     *
     * @param listener the listener to add
     */
    @Override
    public void addGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
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
        Logger.trace("Publish game event: {}", event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}
