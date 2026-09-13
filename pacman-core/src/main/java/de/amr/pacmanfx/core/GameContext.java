/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariantPlayConfig variantPlayConfig;

    private final GameEventManager eventManager;

    private GameSession session;

    public GameContext(CoinMechanism coinMechanism, GameVariantPlayConfig variantPlayConfig, GameEventManager eventManager) {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.variantPlayConfig = requireNonNull(variantPlayConfig);
        this.eventManager = requireNonNull(eventManager);
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public GameSession session() {
        return session;
    }

    public GameVariantPlayConfig variantPlayConfig() {
        return variantPlayConfig;
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public GameEventManager eventManager() {
        return eventManager;
    }

    public AbstractGameState state() {
        return variantPlayConfig.gameFlow().state();
    }
}
