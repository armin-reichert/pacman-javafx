/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.apps;

import de.amr.games.pacman.arcade.ms_pacman.MsPacManGame;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.variants.MsPacManGameConfiguration_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ArcadeMsPacManApp extends Application {

    @Override
    public void init() {
        try {
            File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
            if (userDir.mkdir()) {
                Logger.info("User dir '{}' created", userDir);
            }
            GameController.it().addGameImplementation(GameVariant.MS_PACMAN, new MsPacManGame(userDir));
            GameController.it().selectGame(GameVariant.MS_PACMAN);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.loadAssets();
        ui.setGameConfiguration(GameVariant.MS_PACMAN, new MsPacManGameConfiguration_3D());
        ui.create(stage, initialSize());
        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
