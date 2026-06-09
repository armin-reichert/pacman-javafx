package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.ui.config.UIConfig;

public record GameVariant(
    GameFlow gameFlow,
    AbstractGameModel gameModel,
    GameRules gameRules,
    UIConfig uiConfig
) {}
