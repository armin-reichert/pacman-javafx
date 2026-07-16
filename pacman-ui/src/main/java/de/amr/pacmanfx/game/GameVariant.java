/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.DefaultCheatsImpl;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.simulation.GamePlay;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record GameVariant(
    GamePlay gamePlay,
    GameFlowController gameFlow,
    GameModel gameModel,
    GameCheats cheats,
    GameVariantConfig config,
    Set<GameExtension> extensions,
    Map<Identifier, Object> extensionValues)
{
    public GameVariant(Cartridge cartridge) {
        this(
            cartridge.gamePlayFactory().get(),
            cartridge.gameFlowFactory().get(),
            cartridge.gameModelFactory().get(),
            new DefaultCheatsImpl(),
            cartridge.uiConfigFactory().get(),
            cartridge.gameExtensions(),
            new HashMap<>()
        );
    }

    public <T> T getExtensionValue(GameAppContext actionContext, Identifier id, Class<T> type) {
        final Object cached = extensionValues.get(id);
        if (cached != null) {
            return type.cast(cached);
        }

        final GameExtension ext = extensions.stream()
            .filter(e -> e.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Extension with id " + id + " not found"));

        final Object created = ext.creator().apply(actionContext);
        extensionValues.put(id, created);
        return type.cast(created);
    }
}
