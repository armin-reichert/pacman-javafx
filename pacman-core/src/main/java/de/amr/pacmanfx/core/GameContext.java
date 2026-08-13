/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariantConfig gameVariantConfig;

    private final GameEventManager eventManager;

    private GameSession session;

    public GameContext(
        CoinMechanism coinMechanism,
        GameVariantConfig gameVariantConfig,
        GameEventManager eventManager)
    {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.gameVariantConfig = requireNonNull(gameVariantConfig);
        this.eventManager = requireNonNull(eventManager);
    }

    public void newSession(String variantName, GameFlowController gameFlow, GameCheats cheats) {
        session = new GameSession(variantName, gameFlow, cheats);
    }

    public GameSession session() {
        return session;
    }

    public GameVariantConfig gameVariantConfig() {
        return gameVariantConfig;
    }

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public GameEventManager eventManager() {
        return eventManager;
    }

    public GamePlay gamePlay() {
        return gameVariantConfig.gamePlay();
    }

    public GameRules rules() {
        return gameVariantConfig.gameRules();
    }

    public WorldMapManager worldMapManager() {
        return gameVariantConfig.worldMapManager();
    }

    public GameSystems systems() {
        return gameVariantConfig.systems();
    }
}
