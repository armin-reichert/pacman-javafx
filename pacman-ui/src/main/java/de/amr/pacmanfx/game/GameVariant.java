/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GameVariant {
    private final GameSystems systems;
    private final GamePlay gamePlay;
    private final GameFlowController gameFlow;
    private final GameRules gameRules;
    private final GameModel gameModel;
    private final Supplier<GameCheats> cheatsFactory;
    private final GameVariantConfig config;
    private final Set<GameExtension> extensions;
    private final Map<Named, Object> extensionValues;

    private int initialLifeCount;

    public GameVariant(Cartridge cartridge) {
        systems = cartridge.systemsFactory().get();
        gamePlay = cartridge.gamePlayFactory().get();
        gameFlow = cartridge.gameFlowFactory().get();
        gameRules = cartridge.gameRulesFactory().get();
        gameModel = cartridge.gameModelFactory().get();
        cheatsFactory = GameCheats::new;
        config = cartridge.uiConfigFactory().get();
        extensions = cartridge.gameExtensions();
        extensionValues = new HashMap<>();
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

    public GameModel gameModel() {
        return gameModel;
    }

    public Supplier<GameCheats> cheatsFactory() {
        return cheatsFactory;
    }

    public GameVariantConfig config() {
        return config;
    }

    public Set<GameExtension> extensions() {
        return extensions;
    }

    public Map<Named, Object> extensionValues() {
        return extensionValues;
    }

    public <T> T getExtensionValue(GameAppContext appContext, Named id, Class<T> type) {
        final Object cached = extensionValues.get(id);
        if (cached != null) {
            return type.cast(cached);
        }

        final GameExtension ext = extensions.stream()
            .filter(e -> e.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Extension with id " + id + " not found"));

        final Object created = ext.creator().apply(appContext);
        extensionValues.put(id, created);
        return type.cast(created);
    }
}
