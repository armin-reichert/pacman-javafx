/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameUI;
import de.amr.games.pacman.ui.Globals;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class ArcadeMsPacMan_App extends Application {

    @Override
    public void init() {
        THE_GAME_CONTROLLER.setGameModel(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel());
        THE_GAME_CONTROLLER.selectGameVariant(GameVariant.MS_PACMAN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.2 * height;
        Globals.createUIWith3DSupport();
        THE_UI.configurations().set(GameVariant.MS_PACMAN, new ArcadeMsPacMan_UIConfig());
        THE_UI.build(stage, new Dimension2D(width, height));
        THE_UI.addStartPage(new ArcadeMsPacMan_StartPage(GameVariant.MS_PACMAN));
        THE_UI.buildDashboard(
                GameUI.DashboardID.README,
                GameUI.DashboardID.GENERAL,
                GameUI.DashboardID.GAME_CONTROL,
                GameUI.DashboardID.SETTINGS_3D,
                GameUI.DashboardID.GAME_INFO,
                GameUI.DashboardID.ACTOR_INFO,
                GameUI.DashboardID.KEYBOARD,
                GameUI.DashboardID.ABOUT);
        THE_UI.selectStartPage(0);
        THE_UI.show();
    }
}