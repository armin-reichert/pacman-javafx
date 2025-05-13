/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.PacManGamesEnv;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

public class ArcadeMsPacMan_App extends Application {

    @Override
    public void init() {
        PacManGamesEnv.init();
        theGameController().register(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel());
        theGameController().select(GameVariant.MS_PACMAN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.2 * height;
        createUI(Map.of(GameVariant.MS_PACMAN, ArcadeMsPacMan_UIConfig.class));
        theUI().build(stage, width, height);
        theUI().buildDashboard(
                DashboardID.README,
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD,
                DashboardID.ABOUT);
        theUI().addStartPage(new ArcadeMsPacMan_StartPage(GameVariant.MS_PACMAN));
        theUI().selectStartPage(0);
        theUI().show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}