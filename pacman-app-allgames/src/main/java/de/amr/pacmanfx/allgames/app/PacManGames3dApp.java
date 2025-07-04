/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.PacManGames_UI_Impl;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.ui.PacManGames.theClock;
import static de.amr.pacmanfx.ui.PacManGames.theWatchdog;
import static de.amr.pacmanfx.ui.PacManGames_UI_Impl.*;

/**
 * Application containing all game variants and 3D play scenes.
 */
public class PacManGames3dApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 16:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.6 * height;
        final var xxlSelector = new PacManXXL_Common_MapSelector(CUSTOM_MAP_DIR);
        new PacManGames_UI_Impl.Builder(primaryStage, width, height)
            .game(
                PACMAN,
                ArcadePacMan_GameModel.arcadeVersion(),
                ArcadePacMan_UIConfig.class
            )
            .game(
                MS_PACMAN,
                ArcadeMsPacMan_GameModel.arcadeVersion(),
                ArcadeMsPacMan_UIConfig.class
            )
            .game(
                MS_PACMAN_TENGEN,
                new TengenMsPacMan_GameModel(),
                TengenMsPacMan_UIConfig.class
            )
            .game(
                PACMAN_XXL,
                new PacManXXL_PacMan_GameModel(xxlSelector),
                PacManXXL_PacMan_UIConfig.class
            )
            .game(
                MS_PACMAN_XXL,
                new PacManXXL_MsPacMan_GameModel(xxlSelector),
                PacManXXL_MsPacMan_UIConfig.class
            )
            .startPages(
                new ArcadePacMan_StartPage(PACMAN),
                new ArcadeMsPacMan_StartPage(MS_PACMAN),
                new TengenMsPacMan_StartPage(MS_PACMAN_TENGEN),
                new PacManXXL_Common_StartPage()
            )
            .dashboardEntries(
                DashboardID.GENERAL, DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.ANIMATION_INFO,
                DashboardID.GAME_INFO, DashboardID.ACTOR_INFO,
                DashboardID.CUSTOM_MAPS,
                DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                DashboardID.ABOUT
            )
            .build()
            .show();

        theWatchdog().addEventListener(watchEvents -> xxlSelector.loadCustomMaps());
    }

    @Override
    public void stop() {
        theClock().stop();
        theWatchdog().stopWatching();
    }
}