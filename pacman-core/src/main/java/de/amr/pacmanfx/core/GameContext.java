/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.event.base.GameEventManager;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariantConfig variantConfig;

    private final GameEventManager eventManager;

    private final GameSession session;

    public GameContext(
        CoinMechanism coinMechanism,
        GameVariantConfig variantConfig,
        GameEventManager eventManager,
        GameSession session)
    {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.variantConfig = requireNonNull(variantConfig);
        this.eventManager = requireNonNull(eventManager);
        this.session = requireNonNull(session);
    }

    public GameSession session() {
        return session;
    }

    public GameVariantConfig variantConfig() {
        return variantConfig;
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public GameEventManager eventManager() {
        return eventManager;
    }
}
