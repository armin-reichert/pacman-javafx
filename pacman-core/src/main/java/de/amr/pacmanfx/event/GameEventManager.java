/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.math.Vector2i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class GameEventManager {

    private final GameContext gameContext;
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public GameEventManager(GameContext gameContext) {
        this.gameContext = gameContext;
    }

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

    public void publishEvent(GameEventType type) {
        requireNonNull(type);
        publishEvent(new GameEvent(gameContext.game(), type));
    }

    public void publishEvent(GameEventType type, Vector2i tile) {
        requireNonNull(type);
        publishEvent(new GameEvent(gameContext.game(), type, tile));
    }
}