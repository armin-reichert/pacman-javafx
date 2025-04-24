/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.DashboardID;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.checkIfUserDirectoriesExistAndAreWritable;
import static de.amr.games.pacman.ui.Globals.*;

public class ArcadePacMan_App extends Application {

    @Override
    public void init() {
        checkIfUserDirectoriesExistAndAreWritable();
        THE_GAME_CONTROLLER.registerGameModel(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        THE_GAME_CONTROLLER.gameVariantProperty().set(GameVariant.PACMAN);
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