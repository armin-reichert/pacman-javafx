/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.XXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.XXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.GameBuilder;
import de.amr.pacmanfx.game.PacManGameCollection;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManAllGamesApp extends Application {

    private PacManGameCollection game;
    private boolean includeTests;

    @Override
    public void init() {
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(
                ArcadePacMan_Cartridge.CARTRIDGE,
                ArcadeMsPacMan_Cartridge.CARTRIDGE,
                TengenMsPacMan_Cartridge.CARTRIDGE,
                XXL_PacMan_Cartridge.CARTRIDGE,
                XXL_MsPacMan_Cartridge.CARTRIDGE
            )
            .dashboardFactory(TengenDashboardFactory.instance())
            .startPage(ArcadePacMan_StartPage::new)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .startPage(TengenMsPacMan_StartPage::new)
            .startPage(XXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .build()
            .orElse(null);

        if (game != null) {
            game.watchdog().addEventListener(XXL_MapSelector.instance());
            game.selectGameVariantAndShow(GameVariantID.ARCADE_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}