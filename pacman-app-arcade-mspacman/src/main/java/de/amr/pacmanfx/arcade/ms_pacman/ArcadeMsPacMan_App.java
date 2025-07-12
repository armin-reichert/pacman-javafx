/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.initGameContext;
import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.ui.GameUIContext.theClock;
import static de.amr.pacmanfx.ui.GameUIContext.theWatchdog;
import static de.amr.pacmanfx.ui.PacManGames_UI.GameVariant.MS_PACMAN;

public class ArcadeMsPacMan_App extends Application {

    @Override
    public void init() {
        initGameContext();
    }

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.2 * height;
        PacManGames_UI.build(theGameContext(), primaryStage, width, height)
                .game(
                    MS_PACMAN.name(),
                    ArcadeMsPacMan_GameModel.arcadeVersion(theGameContext()),
                    ArcadeMsPacMan_UIConfig.class
                )
                .startPages(
                    new ArcadeMsPacMan_StartPage(theGameContext(), MS_PACMAN.name())
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
        theWatchdog().dispose();
    }
}