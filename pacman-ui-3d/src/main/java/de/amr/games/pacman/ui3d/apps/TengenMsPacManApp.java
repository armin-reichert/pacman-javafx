/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.apps;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManGameTengen;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.variants.MsPacManGameTengenConfiguration_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration.NES_SIZE;

public class TengenMsPacManApp extends Application {

    private PacManGamesUI_3D ui;

    @Override
    public void init() throws Exception {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        if (userDir.mkdir()) {
            Logger.info("User dir '{}' created", userDir);
        }
        GameController.it().addGameImplementation(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengen(userDir));
        GameController.it().selectGame(GameVariant.MS_PACMAN_TENGEN);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ui = new PacManGamesUI_3D();
        ui.loadAssets();
        var config = new MsPacManGameTengenConfiguration_3D();
        ui.setGameConfiguration(GameVariant.MS_PACMAN_TENGEN, config);
        ui.assets().addAll(config.assets());
        ui.createAndStart(stage, initialSize());
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = (double) NES_SIZE.x() / NES_SIZE.y();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
