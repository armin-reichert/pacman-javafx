/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.PacManGamesCollection;
import de.amr.pacmanfx.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    PacManGamesCollection game;

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(ArcadeMsPacMan_Cartridge.CARTRIDGE)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .build()
            .orElse(null);

        if (game != null) {
            game.showGameVariant(GameVariantID.ARCADE_MS_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}