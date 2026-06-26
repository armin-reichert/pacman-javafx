/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;


import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        new GameBuilder()
            .cartridges(ArcadePacMan_Cartridge.CARTRIDGE)
            .window(primaryStage)
            .startPage(ArcadePacMan_StartPage::new)
            .build()
            .ifPresent(game -> {
                //TODO add extensions automatically
                game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));
                game.showUI(GameVariantID.ARCADE_PACMAN);
            });
    }
}
