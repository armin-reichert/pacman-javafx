/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.Globals.assertNotNull;

public class GameEventManager {

    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public void addEventListener(GameEventListener listener) {
        assertNotNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    public void removeEventListener(GameEventListener listener) {
        assertNotNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    public void publishEvent(GameEvent event) {
        assertNotNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    public void publishEvent(GameModel game, GameEventType type) {
        assertNotNull(game);
        assertNotNull(type);
        publishEvent(new GameEvent(game, type));
    }

    public void publishEvent(GameModel game, GameEventType type, Vector2i tile) {
        assertNotNull(game);
        assertNotNull(type);
        publishEvent(new GameEvent(game, type, tile));
    }
}