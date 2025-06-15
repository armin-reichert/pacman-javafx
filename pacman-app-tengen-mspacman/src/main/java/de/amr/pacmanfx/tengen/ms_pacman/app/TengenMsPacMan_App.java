/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.app;

import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.PacManGames_Env;
import de.amr.pacmanfx.ui.PacManGames_UIBuilder;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;

public class TengenMsPacMan_App extends Application {

    private static final String MS_PACMAN_TENGEN = "MS_PACMAN_TENGEN";

    @Override
    public void start(Stage primaryStage) {
        try {
            // UI size: 80% of available screen height, aspect 16:10
            int height = (int) (0.8 * Screen.getPrimary().getBounds().getHeight());
            int width  = (int) (32.0 / 30.0 * height);
            PacManGames_UIBuilder.buildUI()
                    .game(MS_PACMAN_TENGEN,     new TengenMsPacMan_GameModel())
                    .uiConfig(MS_PACMAN_TENGEN, TengenMsPacMan_UIConfig.class)
                    .startPage(                 new TengenMsPacMan_StartPage(MS_PACMAN_TENGEN))
                    .dashboardEntries(          DashboardID.README,
                                                DashboardID.GENERAL,
                                                DashboardID.GAME_CONTROL,
                                                DashboardID.SETTINGS_3D,
                                                DashboardID.GAME_INFO,
                                                DashboardID.ACTOR_INFO,
                                                DashboardID.KEYBOARD,
                                                DashboardID.ABOUT)
                    .stage(primaryStage, width, height)
                    .selectGame(MS_PACMAN_TENGEN)
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