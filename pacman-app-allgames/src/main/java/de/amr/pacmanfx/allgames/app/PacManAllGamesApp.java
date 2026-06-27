/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.tengenmspacman.TengenDashboardFactory;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.core.GameVariantID.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.core.GameVariantID.ARCADE_PACMAN_XXL;

public class PacManAllGamesApp extends Application {

    Game game;
    boolean includeTests;

    @Override
    public void init() {
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        final PacManXXL_MapSelector sharedMapSelector = new PacManXXL_MapSelector();

        game = new GameBuilder()
            .cartridges(
                ArcadePacMan_Cartridge.CARTRIDGE,
                ArcadeMsPacMan_Cartridge.CARTRIDGE,
                TengenMsPacMan_Cartridge.CARTRIDGE,
                PacManXXL_PacMan_Cartridge.CARTRIDGE,
                PacManXXL_MsPacMan_Cartridge.CARTRIDGE
            )
            .dashboardFactory(TengenDashboardFactory.instance())
            .worldMapSelector(ARCADE_PACMAN_XXL, sharedMapSelector)
            .worldMapSelector(ARCADE_MS_PACMAN_XXL, sharedMapSelector)
            .startPage(ArcadePacMan_StartPage::new)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .startPage(TengenMsPacMan_StartPage::new)
            .startPage(PacManXXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .build()
            .orElse(null);

        if (game != null) {
            //TODO find more elegant solution
            game.watchdog().addEventListener(sharedMapSelector);
            game.showUI(GameVariantID.ARCADE_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}