/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.ui.fx.Settings;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private Settings settings;
    private PacManGames3dUI ui;

    @Override
    public void init() {
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        for (var variant : GameVariants.values()) {
            // initialized by loading class
            Logger.trace("Initialize game variant {}", variant);
        }
        settings = new Settings();
        if (getParameters() != null) {
            settings.merge(getParameters().getNamed());
        }
        Logger.info("Game settings: {}, locale: {}", settings, Locale.getDefault());
        GameController.it().selectGame(settings.variant);
        Logger.info("Game controller initialized. Selected game: {}", GameController.it().game());
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames3dUI(stage, settings);
        for (var game : GameVariants.values()) {
            game.addGameEventListener(ui);
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