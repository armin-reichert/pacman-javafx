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

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    private final PacManGames2dUI ui = new PacManGames2dUI();

    @Override
    public void start(Stage stage) {
        Logger.info("Java   version:   {}", Runtime.version());
        Logger.info("JavaFX version:   {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Locale (default): {}", Locale.getDefault());
        GameController.it().selectGameVariant(GameVariant.PACMAN);
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        ui.loadAssets(true);
        GameSounds.init(ui);
        ui.create(stage, aspect * height, height);
        ui.start();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
    }
}