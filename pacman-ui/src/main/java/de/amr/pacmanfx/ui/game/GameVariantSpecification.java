package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.ui.config.UIConfig;

import java.util.function.Supplier;

public record GameVariantSpecification(
    Supplier<? extends GameFlow> gameFlowFactory,
    Supplier<? extends AbstractGameModel> gameModelFactory,
    Supplier<? extends GameRules> gameRulesFactory,
    Supplier<? extends UIConfig > uiConfigFactory)
{}
