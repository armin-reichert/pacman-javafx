package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;

public record GameVariantImplementation(
    GameFlow gameFlow,
    AbstractGameModel gameModel,
    GameRules gameRules
) {
}
