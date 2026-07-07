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
 * Context for the currently running game variant.
 */
public class GameVariantContext implements GameContext {

    private final PacManGamesCollection game;

    private final GameVariant gameVariant;

    private final GameEventManager eventManager;

    public GameVariantContext(PacManGamesCollection game, GameVariant gameVariant) {
        this.game = requireNonNull(game);
        this.gameVariant = requireNonNull(gameVariant);
        this.eventManager = new EventManager();

        //TODO rethink this
        model().hudState().creditProperty().bind(coinMechanism().numCoinsProperty());
    }

    // --- GameContext

    @Override
    public GameCheats cheats() {
        return gameVariant.cheats();
    }

    @Override
    public CoinMechanism coinMechanism() {
        return game.coinMechanism();
    }

    @Override
    public GameEventManager eventManager() {
        return eventManager;
    }

    @Override
    public GameModel model() {
        return gameVariant.gameModel();
    }

    @Override
    public GameFlow flow() {
        return gameVariant.gameFlow();
    }

    @Override
    public GamePlay gamePlay() {
        return gameVariant.gamePlay();
    }

    // --- GameEventManager

    private static class EventManager implements GameEventManager {

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
}
