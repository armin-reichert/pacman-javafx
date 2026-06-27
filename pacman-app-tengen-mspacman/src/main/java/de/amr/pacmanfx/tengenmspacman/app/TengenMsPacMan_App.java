/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.tengenmspacman.TengenDashboardFactory;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    Game game;

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(TengenMsPacMan_Cartridge.CARTRIDGE)
            .dashboardFactory(TengenDashboardFactory.instance())
            .startPage(TengenMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(NES_SCREEN_ASPECT_RATIO, 0.8)
            .build()
            .orElse(null);

        if (game != null) {
            game.showUI(TENGEN_MS_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}