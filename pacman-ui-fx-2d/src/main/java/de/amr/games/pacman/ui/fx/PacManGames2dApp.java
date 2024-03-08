/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    private final Settings settings = new Settings();
    private PacManGames2dUI ui;

    @Override
    public void init() {
        if (getParameters() != null) {
            settings.merge(getParameters().getNamed());
        }
        GameController.it().newGame(settings.variant);
        Logger.info("Game initialized: {}", settings);
        Logger.info("Java version is {}", Runtime.version());
        Logger.info("JavaFX version is {}", System.getProperty("javafx.runtime.version"));
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames2dUI(stage, settings);
        GameEventManager.addListener(ui);
        ui.showStartPage();
        Logger.info("UI initialized. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
        Logger.info("Theme: {}", ui.theme());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
        Logger.info("Game stopped.");
    }
}