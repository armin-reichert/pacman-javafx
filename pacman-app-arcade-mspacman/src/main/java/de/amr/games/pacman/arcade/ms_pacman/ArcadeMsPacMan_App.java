/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    @Override
    public void init() {
        GameController gameController = GameController.THE_ONE;
        gameController.setGame(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel());
        gameController.games().forEach(GameModel::init);
        gameController.selectGameVariant(GameVariant.MS_PACMAN);
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.configure(GameVariant.MS_PACMAN, new ArcadeMsPacMan_UIConfig(ui.assets()));
        ui.create(stage, initialSize());
        ui.startPageSelectionView().addStartPage(GameVariant.MS_PACMAN, new ArcadeMsPacMan_StartPage(ui));

        ui.gameView().addDefaultDashboardItems(
            "README",
            "GENERAL",
            "GAME_CONTROL",
            "SETTINGS_3D",
            "GAME_INFO",
            "ACTOR_INFO",
            "KEYBOARD",
            "ABOUT"
        );

        ui.stage().show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}