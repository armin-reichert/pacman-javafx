/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameModel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class GameEventManager {

    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public void addEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    public void removeEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    public void publishEvent(GameEvent event) {
        requireNonNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    public void publishEvent(GameModel game, GameEventType type) {
        requireNonNull(game);
        requireNonNull(type);
        publishEvent(new GameEvent(game, type));
    }

    public void publishEvent(GameModel game, GameEventType type, Vector2i tile) {
        requireNonNull(game);
        requireNonNull(type);
        publishEvent(new GameEvent(game, type, tile));
    }
}