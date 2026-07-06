/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.simulation.GamePlay;
import de.amr.pacmanfx.ui.GameVariantConfig;

import java.util.Set;
import java.util.function.Supplier;

public record Cartridge(
    Identifier id,
    Supplier<? extends GamePlay> gamePlayFactory,
    Supplier<? extends GameFlow> gameFlowFactory,
    Supplier<? extends AbstractGameModel> gameModelFactory,
    Supplier<? extends GameVariantConfig> uiConfigFactory,
    Set<GameExtension> gameExtensions)
    implements Identifier
{
    @Override
    public String name() {
        return id.name();
    }
}
