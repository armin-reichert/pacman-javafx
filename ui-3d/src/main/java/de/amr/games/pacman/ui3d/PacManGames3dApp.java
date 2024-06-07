/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.util.List;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private PacManGames3dUI ui;

    @Override
    public void start(Stage stage) {
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight(), width = aspect * height;
        GameController.it().setSupportedVariants(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        GameController.it().selectGameVariant(GameVariant.PACMAN_XXL);
        ui = new PacManGames3dUI();
        ui.loadAssets();
        Logger.info("Assets loaded: {}", ui.theme().summary(List.of(
            new Pair<>(Model3D.class,"3D models"),
            new Pair<>(Image.class, "images"),
            new Pair<>(Font.class, "fonts"),
            new Pair<>(Color.class, "colors"),
            new Pair<>(AudioClip.class, "audio clips")
        )));
        ui.init(stage, width, height);
        for (var variant : GameController.it().supportedVariants()) {
            GameController.it().game(variant).addGameEventListener(ui);
        }
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
        Logger.info("Application stopped.");
    }
}