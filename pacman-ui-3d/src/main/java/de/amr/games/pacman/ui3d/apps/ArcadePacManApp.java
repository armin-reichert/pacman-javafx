package de.amr.games.pacman.ui3d.apps;

import de.amr.games.pacman.arcade.pacman.PacManGame;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.variants.PacManGameConfiguration_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ArcadePacManApp extends Application {

    private PacManGamesUI_3D ui;

    @Override
    public void init() throws Exception {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        if (userDir.mkdir()) {
            Logger.info("User dir '{}' created", userDir);
        }
        GameController.it().addGameImplementation(GameVariant.PACMAN, new PacManGame(userDir));
        GameController.it().selectGame(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ui = new PacManGamesUI_3D();
        ui.loadAssets();
        var config = new PacManGameConfiguration_3D();
        ui.setGameConfiguration(GameVariant.PACMAN, config);
        ui.assets().addAll(config.assets());
        ui.createAndStart(stage, initialSize());
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
