/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.PacManGames_UI_Impl;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.ui.PacManGames.theClock;
import static de.amr.pacmanfx.ui.PacManGames.theWatchdog;
import static de.amr.pacmanfx.ui.PacManGames_UI_Impl.MS_PACMAN;

public class ArcadeMsPacMan_App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.2 * height;
        new PacManGames_UI_Impl.Builder(primaryStage, width, height)
                .game(
                    MS_PACMAN,
                    ArcadeMsPacMan_GameModel.arcadeVersion(),
                    ArcadeMsPacMan_UIConfig.class
                )
                .startPages(
                    new ArcadeMsPacMan_StartPage(MS_PACMAN)
                )
                .dashboardEntries(
                    DashboardID.GENERAL, DashboardID.GAME_CONTROL,
                    DashboardID.SETTINGS_3D,
                    DashboardID.GAME_INFO, DashboardID.ACTOR_INFO,
                    DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                    DashboardID.ABOUT
                )
            .build()
            .show();
    }

    @Override
    public void stop() {
        theClock().stop();
        theWatchdog().stopWatching();
    }
}