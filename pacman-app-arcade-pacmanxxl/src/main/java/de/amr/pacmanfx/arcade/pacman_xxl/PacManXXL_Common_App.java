/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.ui.PacManGames_UIBuilder;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.ui.PacManGames_Env.theClock;

public class PacManXXL_Common_App extends Application {

    private static final String MS_PACMAN_XXL = "MS_PACMAN_XXL";
    private static final String PACMAN_XXL = "PACMAN_XXL";

    @Override
    public void start(Stage primaryStage) {
        try {
            // UI size: 80% of available screen height, aspect 16:10
            int height = (int) (0.8 * Screen.getPrimary().getBounds().getHeight());
            int width  = (int) (1.6 * height);
            var xxlMapSelector = new PacManXXL_Common_MapSelector(CUSTOM_MAP_DIR);
            PacManGames_UIBuilder.buildUI()
                    .game(PACMAN_XXL,           new PacManXXL_PacMan_GameModel(xxlMapSelector))
                    .uiConfig(PACMAN_XXL,       PacManXXL_PacMan_UIConfig.class)
                    .uiConfig(MS_PACMAN_XXL,    PacManXXL_MsPacMan_UIConfig.class)
                    .game(MS_PACMAN_XXL,        new PacManXXL_MsPacMan_GameModel(xxlMapSelector))
                    .startPage(                 new PacManXXL_Common_StartPage())
                    .dashboardEntries(          DashboardID.README,
                            DashboardID.GENERAL,
                            DashboardID.GAME_CONTROL,
                            DashboardID.SETTINGS_3D,
                            DashboardID.GAME_INFO,
                            DashboardID.ACTOR_INFO,
                            DashboardID.CUSTOM_MAPS,
                            DashboardID.KEYBOARD,
                            DashboardID.ABOUT)
                    .stage(primaryStage, width, height)
                    .selectGame(PACMAN_XXL)
                    .show();
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}