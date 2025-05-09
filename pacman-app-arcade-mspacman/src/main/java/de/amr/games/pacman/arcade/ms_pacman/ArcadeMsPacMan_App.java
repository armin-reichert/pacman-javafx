/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.checkUserDirsExistAndWritable;
import static de.amr.games.pacman.ui.Globals.*;

public class ArcadeMsPacMan_App extends Application {

    @Override
    public void init() {
        checkUserDirsExistAndWritable();
        THE_GAME_CONTROLLER.register(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel());
        THE_GAME_CONTROLLER.select(GameVariant.MS_PACMAN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.2 * height;
        createUI(Map.of(GameVariant.MS_PACMAN, ArcadeMsPacMan_UIConfig.class));
        THE_UI.build(stage, width, height);
        THE_UI.addStartPage(new ArcadeMsPacMan_StartPage(GameVariant.MS_PACMAN));
        THE_UI.buildDashboard(
                DashboardID.README,
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD,
                DashboardID.ABOUT);
        THE_UI.selectStartPage(0);
        THE_UI.show();
    }

    @Override
    public void stop() {
        THE_CLOCK.stop();
    }
}