/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.DashboardItemID;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    @Override
    public void init() {
        GameController gameController = GameController.it();
        gameController.setGameModel(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        gameController.gameModels().forEach(GameModel::init);
        gameController.selectGame(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.setGameConfiguration(GameVariant.PACMAN, new ArcadePacMan_GameUIConfig3D(ui.assets()));
        ui.create(stage, initialSize());
        ui.addStartPage(GameVariant.PACMAN, new ArcadePacMan_StartPage(ui));

        ui.gameView().addDashboardItems(
            DashboardItemID.README,
            DashboardItemID.GENERAL,
            DashboardItemID.GAME_CONTROL,
            DashboardItemID.SETTINGS_3D,
            DashboardItemID.GAME_INFO,
            DashboardItemID.ACTOR_INFO,
            DashboardItemID.KEYBOARD,
            DashboardItemID.ABOUT
        );

        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}