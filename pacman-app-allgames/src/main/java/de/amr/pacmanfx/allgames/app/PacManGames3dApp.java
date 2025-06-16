/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.PacManGames_UIBuilder;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.ui.PacManGames_Env.theClock;
import static de.amr.pacmanfx.ui.PacManGames_UIBuilder.buildUI;

/**
 * Application containing all game variants and including 3D play scene.
 *
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 16:10
        final int height = (int) (0.8 * Screen.getPrimary().getBounds().getHeight());
        final int width  = (int) (1.6 * height);
        final var xxlSelector = new PacManXXL_Common_MapSelector(CUSTOM_MAP_DIR);
        buildUI()
            .stage(primaryStage, width, height)
            .game(
                PacManGames_UIBuilder.PACMAN,
                ArcadePacMan_GameModel.arcadeVersion(),
                ArcadePacMan_UIConfig.class)
            .game(
                PacManGames_UIBuilder.MS_PACMAN,
                ArcadeMsPacMan_GameModel.arcadeVersion(),
                ArcadeMsPacMan_UIConfig.class)
            .game(
                PacManGames_UIBuilder.MS_PACMAN_TENGEN,
                new TengenMsPacMan_GameModel(),
                TengenMsPacMan_UIConfig.class)
            .game(PacManGames_UIBuilder.PACMAN_XXL,
                new PacManXXL_PacMan_GameModel(xxlSelector),
                PacManXXL_PacMan_UIConfig.class)
            .game(
                PacManGames_UIBuilder.MS_PACMAN_XXL,
                new PacManXXL_MsPacMan_GameModel(xxlSelector),
                PacManXXL_MsPacMan_UIConfig.class)
            .startPages(
                new ArcadePacMan_StartPage(PacManGames_UIBuilder.PACMAN),
                new ArcadeMsPacMan_StartPage(PacManGames_UIBuilder.MS_PACMAN),
                new TengenMsPacMan_StartPage(PacManGames_UIBuilder.MS_PACMAN_TENGEN),
                new PacManXXL_Common_StartPage())
            .selectStartPage(0)
            .dashboardEntries(
                    DashboardID.GENERAL,
                    DashboardID.GAME_CONTROL,
                    DashboardID.SETTINGS_3D,
                    DashboardID.GAME_INFO,
                    DashboardID.ACTOR_INFO,
                    DashboardID.CUSTOM_MAPS,
                    DashboardID.KEYBOARD,
                    DashboardID.ABOUT)
            .show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}