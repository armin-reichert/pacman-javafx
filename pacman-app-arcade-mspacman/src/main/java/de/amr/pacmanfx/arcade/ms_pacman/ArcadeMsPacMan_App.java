/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.PacManGames_Env;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;

public class ArcadeMsPacMan_App extends Application {

    private static final String MS_PACMAN = "MS_PACMAN";

    @Override
    public void init() {
        PacManGames_Env.init();
        theGameController().registerGame(MS_PACMAN, new ArcadeMsPacMan_GameModel(new ArcadeMsPacMan_MapSelector()));
        theGameController().selectGameVariant(MS_PACMAN);
    }

    @Override
    public void start(Stage stage) {
        initUI(Map.of(MS_PACMAN, ArcadeMsPacMan_UIConfig.class));

        // UI size: 80% of available screen height, aspect 12:10
        double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        theUI().buildUI(stage, 1.2 * height, height,
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.KEYBOARD,
            DashboardID.ABOUT);

        theUI().startPagesView().addStartPage(new ArcadeMsPacMan_StartPage("MS_PACMAN"));
        theUI().startPagesView().selectStartPage(0);

        theUI().show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}