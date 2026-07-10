package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameEventManager;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GameEventManagerImpl implements GameEventManager {

    private final Set<GameEventListener> subscribers = new HashSet<>();

    @Override
    public void addGameEventSubscriber(GameEventListener subscriber) {
        requireNonNull(subscriber);
        final boolean added = subscribers.add(subscriber);
        if (added) {
            Logger.info("{}: Game event subscriber registered: {}", getClass().getSimpleName(), subscriber);
        }
    }

    @Override
    public void removeGameEventSubscriber(GameEventListener subscriber) {
        requireNonNull(subscriber);
        boolean removed = subscribers.remove(subscriber);
        if (removed) {
            Logger.info("{}: Game event subscriber removed: {}", getClass().getSimpleName(), subscriber);
        } else {
            Logger.warn("{}: Game event subscriber not removed (was not registered): {}", getClass().getSimpleName(), subscriber);
        }
    }

    @Override
    public void publishGameEvent(GameEvent event) {
        requireNonNull(event);
        if (Logger.isTraceEnabled()) {
            Logger.trace("Publish game event: {}", event);
        }
        subscribers.forEach(subscriber -> subscriber.onGameEvent(event));
    }
}
