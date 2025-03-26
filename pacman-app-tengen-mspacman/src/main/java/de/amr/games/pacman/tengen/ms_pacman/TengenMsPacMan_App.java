/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameUI;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;

public class TengenMsPacMan_App extends Application {

    @Override
    public void init() {
        THE_GAME_CONTROLLER.setGameModel(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
        THE_GAME_CONTROLLER.games().forEach(GameModel::init);
        THE_GAME_CONTROLLER.selectGameVariant(GameVariant.MS_PACMAN_TENGEN);
    }

    @Override
    public void start(Stage stage) {
        GameUI ui = GameUI.create(Map.of(
            GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_UIConfig()
        ), true);
        ui.create(stage, initialSize());
        ui.addStartPage(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_StartPage());
        ui.addDefaultDashboardItems("README", "GENERAL", "GAME_CONTROL", "SETTINGS_3D", "GAME_INFO",
            "ACTOR_INFO", "JOYPAD", "KEYBOARD", "ABOUT");
        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = (double) NES_SIZE.x() / NES_SIZE.y();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}