/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    Game game;

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(
                PacManXXL_PacMan_Cartridge.CARTRIDGE,
                PacManXXL_MsPacMan_Cartridge.CARTRIDGE)
            .startPage(PacManXXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .build()
            .orElse(null);

        if (game != null) {
            PacManXXL_MapSelector mapSelector = new PacManXXL_MapSelector();
            game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name()).gameModel().setMapSelector(mapSelector);
            game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(mapSelector);
            game.watchdog().addEventListener(mapSelector);

            game.showUI(GameVariantID.ARCADE_PACMAN_XXL);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}