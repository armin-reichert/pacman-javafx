/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.GameBuilder;
import de.amr.pacmanfx.game.PacManGames;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private PacManGames game;

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(
                XXL_PacMan_Cartridge.CARTRIDGE,
                XXL_MsPacMan_Cartridge.CARTRIDGE)
            .startPage(XXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .build()
            .orElse(null);

        if (game != null) {
            game.watchdog().addEventListener(XXL_MapSelector.instance());
            game.selectGameVariantAndShow(GameVariantID.ARCADE_PACMAN_XXL);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}