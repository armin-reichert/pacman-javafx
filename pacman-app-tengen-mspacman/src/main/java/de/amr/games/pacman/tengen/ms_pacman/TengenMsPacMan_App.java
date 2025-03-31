/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameUI;
import de.amr.games.pacman.ui.Globals;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class TengenMsPacMan_App extends Application {

    @Override
    public void init() {
        THE_GAME_CONTROLLER.setGameModel(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
        THE_GAME_CONTROLLER.selectGameVariant(GameVariant.MS_PACMAN_TENGEN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = (double) NES_SIZE.x() / NES_SIZE.y();
        double height = 0.8 * screenSize.getHeight(), width = aspect * height;
        Globals.createUIWith3DSupport();
        THE_UI.configurations().set(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_UIConfig());
        THE_UI.build(stage, new Dimension2D(width, height));
        THE_UI.addStartPage(new TengenMsPacMan_StartPage(GameVariant.MS_PACMAN_TENGEN));
        THE_UI.addDefaultDashboardItems(
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