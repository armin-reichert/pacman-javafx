/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.engine.EngineCoreContext;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.engine.config.Cartridge;
import de.amr.pacmanfx.ui.GameVariantUIConfig;

import static java.util.Objects.requireNonNull;

public final class DefaultGameVariantRuntime implements GameVariantRuntime {

    private final DefaultGameEventManager gameEventManager;

    private final GameSystems systems;

    private final GamePlay gamePlay;

    private final GameFlow gameFlow;

    private final GameRules gameRules;

    private final WorldMapManager worldMapManager;

    private final GameVariantUIConfig uiConfig;

    public DefaultGameVariantRuntime(Cartridge cartridge, EngineCoreContext engine) {
        requireNonNull(cartridge);
        requireNonNull(engine);

        systems = cartridge.systemsFactory().get();
        gamePlay = cartridge.gamePlayFactory().get();
        gameFlow = cartridge.gameFlowFactory().get();
        gameRules = cartridge.gameRulesFactory().get();
        worldMapManager = cartridge.worldMapManagerFactory().get();

        try {
            uiConfig = (GameVariantUIConfig) cartridge.uiConfigClass().getConstructor().newInstance();
        } catch (Exception cause) {
            throw new RuntimeException("Could not create UI configuration from class " + cartridge.uiConfigClass(), cause);
        }

        gameEventManager = new DefaultGameEventManager();
        gameFlow.addStateChangeListener(new StateChangeEventMapper(gameEventManager));
    }

    public GameVariantUIConfig uiConfig() {
        return uiConfig;
    }

    @Override
    public GameSystems systems() {
        return systems;
    }

    @Override
    public DefaultGameEventManager eventManager() {
        return gameEventManager;
    }

    @Override
    public GamePlay gamePlay() {
        return gamePlay;
    }

    @Override
    public GameFlow gameFlow() {
        return gameFlow;
    }

    @Override
    public GameRules gameRules() {
        return gameRules;
    }

    @Override
    public WorldMapManager worldMapManager() {
        return worldMapManager;
    }
}
