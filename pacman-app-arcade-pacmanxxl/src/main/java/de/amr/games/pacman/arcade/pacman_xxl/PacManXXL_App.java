/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.Globals;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class PacManXXL_App extends Application {

    private final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();

    @Override
    public void init() {
        var pacManGameModel = new PacManXXL_PacMan_GameModel(xxlMapSelector);
        var msPacManGameModel = new PacManXXL_MsPacMan_GameModel(xxlMapSelector);
        THE_GAME_CONTROLLER.setGameModel(GameVariant.PACMAN_XXL, pacManGameModel);
        THE_GAME_CONTROLLER.setGameModel(GameVariant.MS_PACMAN_XXL, msPacManGameModel);
        THE_GAME_CONTROLLER.games().forEach(GameModel::init);
        THE_GAME_CONTROLLER.selectGameVariant(GameVariant.MS_PACMAN_XXL);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight(), width = aspect * height;

        Globals.createUIWith3DSupport();

        THE_UI.configurations().set(GameVariant.PACMAN_XXL, new PacManXXL_PacMan_UIConfig());
        THE_UI.configurations().set(GameVariant.MS_PACMAN_XXL, new PacManXXL_MsPacMan_UIConfig());

        THE_UI.build(stage, new Dimension2D(width, height));

        THE_UI.addDefaultDashboardItems("README", "GENERAL", "GAME_CONTROL", "SETTINGS_3D", "GAME_INFO",
            "ACTOR_INFO", "CUSTOM_MAPS", "KEYBOARD", "ABOUT");

        InfoBoxCustomMaps infoBoxCustomMaps = THE_UI.gameView().dashboard().getInfoBox("CUSTOM_MAPS");
        infoBoxCustomMaps.setTableItems(xxlMapSelector.customMaps());

        StartPage xxlStartPage = new PacManXXL_StartPage();
        THE_UI.addStartPage(GameVariant.PACMAN_XXL,    xxlStartPage);
        THE_UI.addStartPage(GameVariant.MS_PACMAN_XXL, xxlStartPage);

        THE_UI.show();
    }
}