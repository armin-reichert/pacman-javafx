/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.pacmanfx.Globals.THE_GAME_CONTROLLER;
import static de.amr.pacmanfx.Globals.checkUserDirsExistAndWritable;
import static de.amr.pacmanfx.ui.Globals.*;

public class ArcadePacMan_App extends Application {

    @Override
    public void init() {
        checkUserDirsExistAndWritable();
        THE_ASSETS.load();
        THE_GAME_CONTROLLER.register(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        THE_GAME_CONTROLLER.select(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        createUI(Map.of(GameVariant.PACMAN, ArcadePacMan_UIConfig.class));
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.2 * height;
        THE_UI.build(stage, width, height);
        THE_UI.addStartPage(new ArcadePacMan_StartPage(GameVariant.PACMAN));
        THE_UI.buildDashboard(
            DashboardID.README,
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.KEYBOARD,
            DashboardID.ABOUT
        );
        THE_UI.selectStartPage(0);
        THE_UI.show();
    }

    @Override
    public void stop() {
        THE_CLOCK.stop();
    }
}