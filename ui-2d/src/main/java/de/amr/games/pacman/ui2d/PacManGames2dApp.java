/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    private Runnable stopAction;

    @Override
    public void start(Stage stage) {
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        GameController.it().setSupportedVariants(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        GameController.it().selectGameVariant(GameVariant.PACMAN);
        var ui = new PacManGames2dUI();
        stopAction = () -> ui.gameClock().stop();
        ui.loadAssets();
        ui.createUI(stage, Screen.getPrimary().getBounds());
        ui.show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        stopAction.run();
    }
}