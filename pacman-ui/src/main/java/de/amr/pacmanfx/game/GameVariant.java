/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;
import org.tinylog.Logger;

import java.util.function.Supplier;

public class GameVariant {
    private final GameSystems systems;
    private final GamePlay gamePlay;
    private final GameFlowController gameFlow;
    private final GameRules gameRules;
    private final WorldMapManager worldMapManager;
    private final Supplier<GameCheats> cheatsFactory;
    private final GameVariantConfig config;

    private int initialLifeCount;

    public GameVariant(Cartridge cartridge) {
        systems = cartridge.systemsFactory().get();
        gamePlay = cartridge.gamePlayFactory().get();
        gameFlow = cartridge.gameFlowFactory().get();
        gameRules = cartridge.gameRulesFactory().get();
        worldMapManager = cartridge.worldMapManagerFactory().get();
        cheatsFactory = GameCheats::new;
        config = cartridge.uiConfigFactory().get();
        initialLifeCount = 3;
    }

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
        Logger.info("Initial life count: {}", initialLifeCount);
    }

    public GameSystems systems() {
        return systems;
    }

    public GamePlay gamePlay() {
        return gamePlay;
    }

    public GameFlowController gameFlow() {
        return gameFlow;
    }

    public GameRules gameRules() {
        return gameRules;
    }

    public WorldMapManager worldMapManager() {
        return worldMapManager;
    }

    public Supplier<GameCheats> cheatsFactory() {
        return cheatsFactory;
    }

    public GameVariantConfig config() {
        return config;
    }

}
