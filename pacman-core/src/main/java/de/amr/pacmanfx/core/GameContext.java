/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.session.GameSession;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContext {

    private final CoinMechanism coinMechanism;

    private final GamePlay gamePlay;

    private final GameSystems systems;

    private final GameModel model;

    private final GameEventManager eventManager;

    private GameSession session;

    public GameContext(CoinMechanism coinMechanism, GamePlay gamePlay, GameSystems systems, GameModel model) {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.gamePlay = requireNonNull(gamePlay);
        this.systems = requireNonNull(systems);
        this.model = requireNonNull(model);
        this.eventManager = new DefaultGameEventManager();
    }

    public void setSession(GameSession session) {
        this.session = requireNonNull(session);
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public GameEventManager eventManager() {
        return eventManager;
    }

    public GamePlay gamePlay() {
        return gamePlay;
    }

    public GameModel model() {
        return model;
    }

    public GameSession session() {
        return session;
    }

    public GameSystems systems() {
        return systems;
    }
}
