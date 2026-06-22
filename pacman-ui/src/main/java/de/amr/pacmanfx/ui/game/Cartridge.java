/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.ui.config.GameUIConfig;

import java.util.function.Supplier;

public record Cartridge(
    Identifier id,
    Supplier<? extends GameFlow> gameFlowFactory,
    Supplier<? extends AbstractGameModel> gameModelFactory,
    Supplier<? extends GameRules> gameRulesFactory,
    Supplier<? extends GameUIConfig> uiConfigFactory) implements Identifier
{
    @Override
    public String name() {
        return id.name();
    }
}
