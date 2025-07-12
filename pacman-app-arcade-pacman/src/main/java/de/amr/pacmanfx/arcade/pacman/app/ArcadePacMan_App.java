/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.initGameContext;
import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.ui.GameUIContext.theClock;
import static de.amr.pacmanfx.ui.GameUIContext.theWatchdog;

public class ArcadePacMan_App extends Application {

    @Override
    public void init() {
        initGameContext();
    }

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.2 * height;
        var gameVariant = PacManGames_UI.GameVariant.PACMAN.name();
        PacManGames_UI.build(primaryStage, width, height)
            .game(
                gameVariant,
                ArcadePacMan_GameModel.arcadeVersion(theGameContext()),
                ArcadePacMan_UIConfig.class
            )
            .startPages(
                new ArcadePacMan_StartPage(theGameContext(), gameVariant)
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