/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventManager;
import de.amr.games.pacman.ui.fx.Settings;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private final Settings settings = new Settings();
    private PacManGames3dUI ui;

    @Override
    public void init() {
        if (getParameters() != null) {
            settings.merge(getParameters().getNamed());
        }
        GameController.create(settings.variant);
        Logger.info("Game initialized: {}, locale: {}", settings, Locale.getDefault());
        Logger.info("Java version is {}", System.getProperty("java.version"));
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames3dUI(stage, settings);
        GameEventManager.addListener(ui);
        ui.showStartPage();
        Logger.info("UI created. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
        Logger.info("Theme: {}", ui.theme());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
        Logger.info("Game stopped.");
    }
}