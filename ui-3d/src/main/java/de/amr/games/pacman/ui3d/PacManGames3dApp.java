/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

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
public class PacManGames3dApp extends Application {

    private PacManGames3dUI ui;

    @Override
    public void init() {
        GameController.it().setSupportedGameVariants(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        GameController.it().selectGame(GameVariant.PACMAN_XXL);
        Logger.info("Game controller initialized. Selected game: {}", GameController.it().game().variant());
        Logger.info("Java version:   {}", Runtime.version());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        PacManGames3dUI.addAssets3D();
        Logger.info("3D assets added to 2D theme.");
    }

    @Override
    public void start(Stage stage) {
        try {
            Rectangle2D screenSize = Screen.getPrimary().getBounds();
            double aspect = screenSize.getWidth() / screenSize.getHeight();
            double height = 0.8 * screenSize.getHeight(), width = aspect * height;
            ui = new PacManGames3dUI(stage, width, height);
            for (var variant : GameController.it().supportedGameVariants()) {
                GameController.it().game(variant).addGameEventListener(ui);
            }
            ui.showStartPage();
            Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
        Logger.info("Application stopped.");
    }
}