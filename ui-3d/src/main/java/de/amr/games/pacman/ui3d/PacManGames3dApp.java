/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_ENABLED;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private PacManGames3dUI ui;

    @Override
    public void start(Stage stage) {
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        GameController.it().setSupportedVariants(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        GameController.it().selectGameVariant(GameVariant.PACMAN);
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        ui = new PacManGames3dUI(stage, bounds.getWidth(), bounds.getHeight());
        ui.show();
        PY_3D_ENABLED.set(true);
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
    }
}