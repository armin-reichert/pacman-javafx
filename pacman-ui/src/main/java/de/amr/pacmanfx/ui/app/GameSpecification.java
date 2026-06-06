package de.amr.pacmanfx.ui.app;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;

public record GameSpecification(
    AbstractGameModel gameModel,
    GameFlow gameFlow,
    GameRules gameRules)
{}
