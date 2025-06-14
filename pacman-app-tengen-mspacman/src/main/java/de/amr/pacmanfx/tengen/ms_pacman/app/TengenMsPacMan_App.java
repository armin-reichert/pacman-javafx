/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.app;

import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.PacManGames_Env;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;

public class TengenMsPacMan_App extends Application {

    @Override
    public void init() {
        PacManGames_Env.init();
        theGameController().registerGame("MS_PACMAN_TENGEN", new TengenMsPacMan_GameModel());
        theGameController().selectGameVariant("MS_PACMAN_TENGEN");
    }

    @Override
    public void start(Stage stage) {
        createUI(Map.of("MS_PACMAN_TENGEN", TengenMsPacMan_UIConfig.class));

        // UI size: 80% of available screen height, aspect 32:30
        double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        double aspect = (double) NES_TILES.x() / NES_TILES.y();
        theUI().buildUI(stage, aspect * height, height,
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.KEYBOARD,
            DashboardID.ABOUT);

        theUI().startPagesView().addStartPage(new TengenMsPacMan_StartPage("MS_PACMAN_TENGEN"));
        theUI().startPagesView().selectStartPage(0);

        theUI().show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}