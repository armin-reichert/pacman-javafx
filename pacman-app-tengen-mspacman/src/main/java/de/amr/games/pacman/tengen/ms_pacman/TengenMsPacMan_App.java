/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;

public class TengenMsPacMan_App extends Application {

    @Override
    public void init() {
        GameController gameController = GameController.THE_ONE;
        try {
            gameController.setGame(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
            gameController.games().forEach(GameModel::init);
            gameController.selectGameVariant(GameVariant.MS_PACMAN_TENGEN);
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.configure(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_UIConfig(ui.assets()));
        ui.create(stage, initialSize());
        ui.startPageSelectionView().addStartPage(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_StartPage(ui));

        ui.gameView().addDefaultDashboardItems(
            "README",
            "GENERAL",
            "GAME_CONTROL",
            "SETTINGS_3D",
            "GAME_INFO",
            "ACTOR_INFO",
            "JOYPAD",
            "KEYBOARD",
            "ABOUT"
        );

        stage.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = (double) NES_SIZE.x() / NES_SIZE.y();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}