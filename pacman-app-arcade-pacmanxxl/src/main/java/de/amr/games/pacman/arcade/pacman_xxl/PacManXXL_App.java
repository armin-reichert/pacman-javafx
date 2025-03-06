/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.DashboardItemID;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();

    @Override
    public void init() {
        var pacManGameModel = new PacManXXL_PacMan_GameModel(xxlMapSelector);
        var msPacManGameModel = new PacManXXL_MsPacMan_GameModel(xxlMapSelector);
        GameController gameController = GameController.it();
        gameController.setGameModel(GameVariant.PACMAN_XXL, pacManGameModel);
        gameController.setGameModel(GameVariant.MS_PACMAN_XXL, msPacManGameModel);
        gameController.gameModels().forEach(GameModel::init);
        gameController.selectGame(GameVariant.MS_PACMAN_XXL);
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.loadAssets();

        ui.setGameConfiguration(GameVariant.PACMAN_XXL,    new PacManXXL_PacMan_GameConfig3D(ui.assets()));
        ui.setGameConfiguration(GameVariant.MS_PACMAN_XXL, new PacManXXL_MsPacMan_GameConfig3D(ui.assets()));

        ui.create(stage, initialSize());

        ui.gameView().addDashboardItems(
            DashboardItemID.README,
            DashboardItemID.GENERAL,
            DashboardItemID.GAME_CONTROL,
            DashboardItemID.SETTINGS_3D,
            DashboardItemID.GAME_INFO,
            DashboardItemID.ACTOR_INFO,
            DashboardItemID.CUSTOM_MAPS,
            DashboardItemID.KEYBOARD,
            DashboardItemID.ABOUT
        );

        InfoBoxCustomMaps infoBoxCustomMaps = ui.getDashboardItem(DashboardItemID.CUSTOM_MAPS);
        infoBoxCustomMaps.getMapsTableView().setItems(xxlMapSelector.customMaps());

        StartPage xxlStartPage = new PacManXXL_StartPage(ui);
        ui.addStartPage(GameVariant.PACMAN_XXL,    xxlStartPage);
        ui.addStartPage(GameVariant.MS_PACMAN_XXL, xxlStartPage);

        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}