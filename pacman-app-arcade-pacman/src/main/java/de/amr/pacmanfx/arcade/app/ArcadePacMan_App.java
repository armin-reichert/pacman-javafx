/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.app;

import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.ui.PacManGames_Env.theClock;
import static de.amr.pacmanfx.ui.PacManGames_UIBuilder.buildUI;

public class ArcadePacMan_App extends Application {

    public static final String PACMAN = "PACMAN";

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        final int height = (int) (0.8 * Screen.getPrimary().getBounds().getHeight());
        final int width  = (int) (1.2 * height);
        buildUI()
            .stage(primaryStage, width, height)
            .game(PACMAN, ArcadePacMan_GameModel.arcadeVersion(), ArcadePacMan_UIConfig.class)
            .startPages(new ArcadePacMan_StartPage(PACMAN))
            .dashboardEntries(
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD,
                DashboardID.ABOUT)
            .selectGame(PACMAN)
            .show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}