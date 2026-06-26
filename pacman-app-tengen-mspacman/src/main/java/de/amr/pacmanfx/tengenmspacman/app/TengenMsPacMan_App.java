/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.tengenmspacman.*;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    static final float ASPECT_RATIO    = NES_SCREEN_ASPECT_RATIO; // 32:30
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of available height

    Game game;

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(TengenMsPacMan_Cartridge.CARTRIDGE)
            .dashboardFactory(TengenDashboardFactory.instance())
            .startPage(TengenMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(ASPECT_RATIO, HEIGHT_FRACTION)
            .build();

        game.extensions().add(TengenMsPacMan_GameExtension.UI_SETTINGS,
            new TengenMsPacMan_UISettings());

        game.extensions().add(TengenMsPacMan_GameExtension.ACTIONS,
            new TengenMsPacMan_Actions(game));

        game.showUI(TENGEN_MS_PACMAN);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}