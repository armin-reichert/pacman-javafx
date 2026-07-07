/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.DefaultCheatsImpl;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.simulation.GamePlay;
import de.amr.pacmanfx.ui.GameVariantConfig;

public record GameVariant(
    GamePlay gamePlay,
    GameFlow gameFlow,
    GameModel gameModel,
    GameCheats cheats,
    GameVariantConfig config)
{
    public GameVariant(Cartridge cartridge) {
        this(
            cartridge.gamePlayFactory().get(),
            cartridge.gameFlowFactory().get(),
            cartridge.gameModelFactory().get(),
            new DefaultCheatsImpl(),
            cartridge.uiConfigFactory().get()
        );
    }
}
