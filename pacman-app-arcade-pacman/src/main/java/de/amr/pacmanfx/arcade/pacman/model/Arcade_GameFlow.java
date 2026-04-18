/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Arcade_GameFlow extends StateMachine<Game> implements GameFlow {

    private static void ensureFxThread(String actionDesc) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(actionDesc + " must be executed on the JavaFX Application Thread");
        }
    }

    private final BooleanProperty cutScenesEnabled = new SimpleBooleanProperty(true);
    private final Set<GameEventListener> eventListeners = new HashSet<>();

    public Arcade_GameFlow(Arcade_GameModel game) {
        setName("Arcade Pac-Man Game Flow");
        setContext(game);
        addStateChangeListener((oldState, newState) -> publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
        Stream.of(Arcade_GameState.values()).forEach(this::addState);
    }

    /**
     * Registers a {@link GameEventListener}.
     *
     * @param listener the listener to add
     */
    @Override
    public void addGameEventListener(GameEventListener listener) {
        requireNonNull(listener);
        ensureFxThread("Adding game event listeners");
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
        ensureFxThread("Removing game event listeners");
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
