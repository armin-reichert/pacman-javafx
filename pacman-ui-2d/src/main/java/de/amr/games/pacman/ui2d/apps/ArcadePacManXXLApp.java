package de.amr.games.pacman.ui2d.apps;

import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXL;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXLConfiguration;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ArcadePacManXXLApp extends Application {

    private PacManGamesUI ui;

    @Override
    public void init() throws Exception {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        if (userDir.mkdir()) {
            Logger.info("User dir '{}' created", userDir);
        }
        GameController.it().addGameImplementation(GameVariant.PACMAN_XXL, new PacManGameXXL(userDir));
        GameController.it().selectGame(GameVariant.PACMAN_XXL);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ui = new PacManGamesUI();
        ui.loadAssets();
        var config = new PacManGameXXLConfiguration();
        ui.setGameConfiguration(GameVariant.PACMAN_XXL, config);
        ui.assets().addAll(config.assets());
        ui.createAndStart(stage, initialSize());
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
