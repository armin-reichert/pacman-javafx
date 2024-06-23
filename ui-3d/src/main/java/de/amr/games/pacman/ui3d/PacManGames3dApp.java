/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameClock;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private GameClock clock;

    @Override
    public void start(Stage stage) {
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        GameController.it().setSupportedVariants(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        GameController.it().selectGameVariant(GameVariant.PACMAN);
        var ui = new PacManGames3dUI();
        PacManGames3dUI.PY_3D_ENABLED.set(true);
        try {
            ui.loadAssets();
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
        ui.createUI(stage, Screen.getPrimary().getBounds());
        ui.show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
        clock = ui.gameClock();
    }

    @Override
    public void stop() {
        clock.stop();
    }
}