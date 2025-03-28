/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.Globals;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class ArcadePacMan_App extends Application {

    @Override
    public void init() {
        THE_GAME_CONTROLLER.setGameModel(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        THE_GAME_CONTROLLER.games().forEach(GameModel::init);
        THE_GAME_CONTROLLER.selectGameVariant(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.2 * height;
        Globals.createUIWith3DSupport();
        THE_UI.configurations().set(GameVariant.PACMAN, new ArcadePacMan_UIConfig());
        THE_UI.build(stage, new Dimension2D(width, height));
        THE_UI.addStartPage(GameVariant.PACMAN, new ArcadePacMan_StartPage());
        THE_UI.addDefaultDashboardItems("README", "GENERAL", "GAME_CONTROL", "SETTINGS_3D", "GAME_INFO", "ACTOR_INFO", "KEYBOARD", "ABOUT");
        THE_UI.show();
    }
}