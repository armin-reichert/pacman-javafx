/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameUI;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.*;

public class ArcadePacMan_App extends Application {

    @Override
    public void init() {
        THE_GAME_CONTROLLER.setGameModel(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        THE_GAME_CONTROLLER.selectGameVariant(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.2 * height;
        createUIAndSupport3D(true, Map.of(GameVariant.PACMAN, ArcadePacMan_UIConfig.class));
        THE_UI.build(stage, new Dimension2D(width, height));
        THE_UI.addStartPage(new ArcadePacMan_StartPage(GameVariant.PACMAN));
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

    @Override
    public void stop() {
        THE_CLOCK.stop();
    }
}