package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.ui.GameVariantConfig;

public record GameVariant(
    GameFlow gameFlow,
    AbstractGameModel gameModel,
    GameRules gameRules,
    GameVariantConfig uiConfig)
{
    public GameVariant(Cartridge cartridge) {
        this(
            cartridge.gameFlowFactory().get(),
            cartridge.gameModelFactory().get(),
            cartridge.gameRulesFactory().get(),
            cartridge.uiConfigFactory().get()
        );
    }
}
