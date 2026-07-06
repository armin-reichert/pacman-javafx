/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.simulation.GamePlay;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Context for the currently selected game variant.
 */
public class GameContextImpl implements GameContext, GameEventManager {

    private final PacManGamesCollection game;

    private final GameVariant gameVariant;

    private final Set<GameEventListener> eventListeners = new HashSet<>();

    public GameContextImpl(PacManGamesCollection game, GameVariant gameVariant) {
        this.game = requireNonNull(game);
        this.gameVariant = requireNonNull(gameVariant);
    }

    @Override
    public GameCheats cheats() {
        return gameVariant.cheats();
    }

    @Override
    public CoinMechanism coinMechanism() {
        return game.coinMechanism();
    }

    @Override
    public GameModel model() {
        return gameVariant.gameModel();
    }

    @Override
    public GamePlay gamePlay() {
        return gameVariant.gamePlay();
    }

    @Override
    public GameEventManager eventManager() {
        return this;
    }

    @Override
    public GameFlow flow() {
        return gameVariant.gameFlow();
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
}
