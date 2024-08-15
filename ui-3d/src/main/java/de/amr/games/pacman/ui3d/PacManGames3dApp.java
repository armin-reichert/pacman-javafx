/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.GameSounds;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Locale;

import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_ENABLED;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private final PacManGames3dUI ui = new PacManGames3dUI();

    @Override
    public void start(Stage stage) {
        Logger.info("Java   version:   {}", Runtime.version());
        Logger.info("JavaFX version:   {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Locale (default): {}", Locale.getDefault());
        GameController.it().selectGameVariant(GameVariant.PACMAN);
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        ui.loadAssets(true);
        GameSounds.init(ui.assets(), ui);
        ui.create(stage, width, height);
        ui.start();
        PY_3D_ENABLED.set(true);
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
    }
}