/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.PacManGames_UIBuilder;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.pacmanfx.ui.PacManGames_Env.theClock;

public class ArcadeMsPacMan_App extends Application {

    public static final String MS_PACMAN = "MS_PACMAN";

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        int height = (int) (0.8 * Screen.getPrimary().getBounds().getHeight());
        int width  = (int) (1.2 * height);
        try {
            PacManGames_UIBuilder.buildUI()
                    .game(MS_PACMAN, new ArcadeMsPacMan_GameModel(new ArcadeMsPacMan_MapSelector()))
                    .uiConfig(MS_PACMAN, ArcadeMsPacMan_UIConfig.class)
                    .startPage(new ArcadeMsPacMan_StartPage(MS_PACMAN))
                    .dashboardEntries(
                            DashboardID.GENERAL,
                            DashboardID.GAME_CONTROL,
                            DashboardID.SETTINGS_3D,
                            DashboardID.GAME_INFO,
                            DashboardID.ACTOR_INFO,
                            DashboardID.KEYBOARD,
                            DashboardID.ABOUT)
                    .stage(primaryStage, width, height)
                    .selectGame(MS_PACMAN)
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