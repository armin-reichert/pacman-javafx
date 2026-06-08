package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;

import java.util.function.Supplier;

public record GameVariantSpecification(
    Supplier<? extends GameFlow> gameFlowFactory,
    AbstractGameModel gameModel,
    GameRules gameRules,
    boolean includeTests)
{}
