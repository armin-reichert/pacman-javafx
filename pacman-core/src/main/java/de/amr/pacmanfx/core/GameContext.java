/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.model.GameCheats;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariantConfig variantConfig;

    private final GameEventManager eventManager;

    private GameSession session;

    public GameContext(
        CoinMechanism coinMechanism,
        GameVariantConfig variantConfig,
        GameEventManager eventManager)
    {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.variantConfig = requireNonNull(variantConfig);
        this.eventManager = requireNonNull(eventManager);
    }

    public void newSession(String variantName, GameFlowController gameFlow, GameCheats cheats) {
        session = new GameSession(variantName, gameFlow, cheats);
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
