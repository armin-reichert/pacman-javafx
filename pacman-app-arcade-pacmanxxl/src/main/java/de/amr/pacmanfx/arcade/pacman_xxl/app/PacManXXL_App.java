/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    static final double ASPECT_RATIO    = 1.6;
    static final double HEIGHT_FRACTION = 0.8;

    Game game;

    @Override
    public void start(Stage stage) {
        new GameBuilder()
            .cartridges(
                PacManXXL_PacMan_Cartridge.CARTRIDGE,
                PacManXXL_MsPacMan_Cartridge.CARTRIDGE)
            .startPage(PacManXXL_StartPage::new)
            .window(stage)
            .screenArea(ASPECT_RATIO, HEIGHT_FRACTION)
            .build()
            .ifPresent(game -> {
                this.game = game;

                PacManXXL_MapSelector sharedMapSelector = new PacManXXL_MapSelector();
                game.watchdog().addEventListener(sharedMapSelector);
                game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())
                    .gameModel().setMapSelector(sharedMapSelector);
                game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name())
                    .gameModel().setMapSelector(sharedMapSelector);

                game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));
                game.showUI(GameVariantID.ARCADE_PACMAN_XXL);
            });
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}