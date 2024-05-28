/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    private PacManGames2dUI ui;

    @Override
    public void init() {
        GameController.it().setSupportedGameVariants(GameVariant.PACMAN, GameVariant.MS_PACMAN);
        GameController.it().selectGame(GameVariant.PACMAN);
        Logger.info("Game controller initialized. Selected game: {}", GameController.it().game().variant());
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames2dUI(stage);
        for (var variant : GameController.it().supportedGameVariants()) {
             GameController.it().game(variant).addGameEventListener(ui);
        }
        ui.showStartPage();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
        Logger.info("Application stopped.");
    }
}