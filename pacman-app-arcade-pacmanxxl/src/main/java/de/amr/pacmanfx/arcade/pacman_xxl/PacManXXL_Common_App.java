/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.GameVariant.MS_PACMAN_XXL;
import static de.amr.pacmanfx.ui.GameVariant.PACMAN_XXL;

public class PacManXXL_Common_App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 16:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.6 * height;
        var xxlMapSelector = new PacManXXL_Common_MapSelector(theGameContext().theCustomMapDir());
        GameUI.build(theGameContext(), primaryStage, width, height)
                .game(
                    PACMAN_XXL.name(),
                    PacManXXL_PacMan_GameModel.class,
                    xxlMapSelector,
                    PacManXXL_PacMan_UIConfig.class
                )
                .game(
                    MS_PACMAN_XXL.name(),
                    PacManXXL_MsPacMan_GameModel.class,
                    xxlMapSelector,
                    PacManXXL_MsPacMan_UIConfig.class
                )
                .startPages(
                    new PacManXXL_Common_StartPage(theGameContext())
                )
                .dashboardEntries(
                    DashboardID.README, DashboardID.GENERAL,
                    DashboardID.GAME_CONTROL, DashboardID.SETTINGS_3D,
                    DashboardID.GAME_INFO, DashboardID.ACTOR_INFO, DashboardID.CUSTOM_MAPS,
                    DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                    DashboardID.ABOUT
                )
            .build()
            .show();

        theUI().theWatchdog().addEventListener(watchEvents -> xxlMapSelector.loadCustomMaps());
    }

    @Override
    public void stop() {
        theUI().theGameClock().stop();
        theUI().theWatchdog().dispose();
    }
}