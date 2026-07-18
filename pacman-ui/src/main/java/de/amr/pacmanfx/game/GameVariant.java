/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.DefaultCheatsImpl;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameVariant {
    private final GamePlay gamePlay;
    private final GameFlowController gameFlow;
    private final GameModel gameModel;
    private final GameCheats cheats;
    private final GameVariantConfig config;
    private final Set<GameExtension> extensions;
    private final Map<Identifier, Object> extensionValues;

    public GameVariant(Cartridge cartridge) {
        gamePlay = cartridge.gamePlayFactory().get();
        gameFlow = cartridge.gameFlowFactory().get();
        gameModel = cartridge.gameModelFactory().get();
        cheats = new DefaultCheatsImpl();
        config = cartridge.uiConfigFactory().get();
        extensions = cartridge.gameExtensions();
        extensionValues = new HashMap<>();
    }

    public GamePlay gamePlay() {
        return gamePlay;
    }

    public GameFlowController gameFlow() {
        return gameFlow;
    }

    public GameModel gameModel() {
        return gameModel;
    }

    public GameCheats cheats() {
        return cheats;
    }

    public GameVariantConfig config() {
        return config;
    }

    public Set<GameExtension> extensions() {
        return extensions;
    }

    public Map<Identifier, Object> extensionValues() {
        return extensionValues;
    }

    public <T> T getExtensionValue(GameAppContext appContext, Identifier id, Class<T> type) {
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
