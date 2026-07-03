package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.DefaultCheatsImpl;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.ui.GameVariant;

public record GameVariantRuntime(
    GameFlow gameFlow,
    AbstractGameModel gameModel,
    GameCheats cheats,
    GameVariant gameVariant)
{
    public GameVariantRuntime(Cartridge cartridge) {
        this(
            cartridge.gameFlowFactory().get(),
            cartridge.gameModelFactory().get(),
            new DefaultCheatsImpl(),
            cartridge.uiConfigFactory().get()
        );
        gameModel.setRules(cartridge.gameRulesFactory().get());
    }
}
