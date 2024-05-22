/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.Settings;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private PacManGames3dUI ui;

    @Override
    public void init() {
        GameController.it().setSupportedGameVariants(new GameVariant[] {
            GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL
        });
        GameController.it().selectGame(GameVariant.PACMAN_XXL);
        Logger.info("Game controller initialized. Selected game: {}", GameController.it().game().variant());
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames3dUI(stage);
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