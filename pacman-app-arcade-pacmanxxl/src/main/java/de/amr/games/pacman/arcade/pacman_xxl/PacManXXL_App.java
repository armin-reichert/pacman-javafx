/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.dashboard.DashboardItemID;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

public class PacManXXL_App extends Application {

    private PacManXXL_PacMan_GameModel pacManGameModel;
    private PacManXXL_MsPacMan_GameModel msPacManGameModel;

    @Override
    public void init() {
        GameController gameController = GameController.it();
        pacManGameModel = new PacManXXL_PacMan_GameModel();
        gameController.setGameModel(GameVariant.PACMAN_XXL, pacManGameModel);
        msPacManGameModel = new PacManXXL_MsPacMan_GameModel();
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

        ui.addDashboardItem(DashboardItemID.README);
        ui.addDashboardItem(DashboardItemID.GENERAL);
        ui.addDashboardItem(DashboardItemID.GAME_CONTROL);
        ui.addDashboardItem(ui.locText("infobox.3D_settings.title"), new InfoBox3D());
        ui.addDashboardItem(DashboardItemID.GAME_INFO);
        ui.addDashboardItem(DashboardItemID.ACTOR_INFO);

        InfoBoxCustomMaps infoBoxCustomMaps = new InfoBoxCustomMaps();
        infoBoxCustomMaps.getMapsTableView().setItems(pacManGameModel.mapSelector().customMaps());
        ui.addDashboardItem(ui.locText("infobox.custom_maps.title"), infoBoxCustomMaps);

        ui.addDashboardItem(DashboardItemID.KEYBOARD);
        ui.addDashboardItem(DashboardItemID.ABOUT);

        StartPage xxlStartPage = new PacManXXL_StartPage(ui);
        ui.setStartPage(GameVariant.PACMAN_XXL,    xxlStartPage);
        ui.setStartPage(GameVariant.MS_PACMAN_XXL, xxlStartPage);

        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}