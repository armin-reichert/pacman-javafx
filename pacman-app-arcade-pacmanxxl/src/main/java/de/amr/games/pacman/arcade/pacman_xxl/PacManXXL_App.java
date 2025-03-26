/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameUI;
import de.amr.games.pacman.ui.UIGlobals;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;

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
        GameUI ui = UIGlobals.createGameUI(Map.of(
            GameVariant.PACMAN_XXL, new PacManXXL_PacMan_UIConfig(),
            GameVariant.MS_PACMAN_XXL, new PacManXXL_MsPacMan_UIConfig()
        ), true);
        ui.create(stage, initialSize());
        ui.addDefaultDashboardItems("README", "GENERAL", "GAME_CONTROL", "SETTINGS_3D", "GAME_INFO",
            "ACTOR_INFO", "CUSTOM_MAPS", "KEYBOARD", "ABOUT");

        InfoBoxCustomMaps infoBoxCustomMaps = ui.gameView().dashboard().getItem("CUSTOM_MAPS");
        infoBoxCustomMaps.setTableItems(xxlMapSelector.customMaps());

        StartPage xxlStartPage = new PacManXXL_StartPage();
        ui.startPageSelectionView().addStartPage(GameVariant.PACMAN_XXL,    xxlStartPage);
        ui.startPageSelectionView().addStartPage(GameVariant.MS_PACMAN_XXL, xxlStartPage);

        stage.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}