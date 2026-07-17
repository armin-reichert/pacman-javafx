/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.gameplay.GamePlay;

import java.util.Set;
import java.util.function.Supplier;

public record Cartridge(
    Identifier id,
    Supplier<? extends GamePlay> gamePlayFactory,
    Supplier<? extends GameFlowController> gameFlowFactory,
    Supplier<? extends GameModel> gameModelFactory,
    Supplier<? extends GameVariantConfig> uiConfigFactory,
    Set<GameExtension> gameExtensions)
    implements Identifier
{
    @Override
    public String name() {
        return id.name();
    }
}
