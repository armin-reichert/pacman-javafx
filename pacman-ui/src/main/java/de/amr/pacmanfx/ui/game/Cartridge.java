/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.ui.GameVariantConfig;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record Cartridge(
    Identifier id,
    Supplier<? extends GameFlow> gameFlowFactory,
    Supplier<? extends AbstractGameModel> gameModelFactory,
    Supplier<? extends GameRules> gameRulesFactory,
    Supplier<? extends GameVariantConfig> uiConfigFactory,
    Map<Identifier, Function<Game, Object>> gameExtensionSuppliers)
    implements Identifier
{
    @Override
    public String name() {
        return id.name();
    }
}
