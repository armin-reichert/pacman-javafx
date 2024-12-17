/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.arcade.ms_pacman.MsPacManGame;
import de.amr.games.pacman.arcade.pacman.PacManGame;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXL;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.ui3d.model.Model3D;
import de.amr.games.pacman.ui3d.scene3d.Perspective;
import de.amr.games.pacman.ui3d.variants.MsPacManGameConfiguration_3D;
import de.amr.games.pacman.ui3d.variants.MsPacManGameTengenConfiguration_3D;
import de.amr.games.pacman.ui3d.variants.PacManGameConfiguration_3D;
import de.amr.games.pacman.ui3d.variants.PacManGameXXLConfiguration_3D;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    public static final BooleanProperty             PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode>    PY_3D_DRAW_MODE          = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(false);
    public static final BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.web("#202020"));
    public static final StringProperty              PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty(PacManGamesUI_3D.NO_TEXTURE);
    public static final ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.GHOSTWHITE);
    public static final BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Perspective.Name> PY_3D_PERSPECTIVE   = new SimpleObjectProperty<>(Perspective.Name.TOTAL);
    public static final DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    public static final DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(0.9);

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.6;
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        return new Dimension2D(width, height);
    }

    private PacManGamesUI_3D ui;

    @Override
    public void init() {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        if (userDir.mkdir()) {
            Logger.info("User dir '{}' created", userDir);
        }
        GameController.it().addGameImplementation(GameVariant.MS_PACMAN, new MsPacManGame(userDir));
        GameController.it().addGameImplementation(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengen(userDir));
        GameController.it().addGameImplementation(GameVariant.PACMAN, new PacManGame(userDir));
        GameController.it().addGameImplementation(GameVariant.PACMAN_XXL, new PacManGameXXL(userDir));
        GameController.it().selectGame(GameVariant.PACMAN);
        PY_3D_ENABLED.set(false);
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGamesUI_3D();
        ui.loadAssets();
        ui.setGameConfiguration(GameVariant.MS_PACMAN, new MsPacManGameConfiguration_3D());
        ui.setGameConfiguration(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengenConfiguration_3D());
        ui.setGameConfiguration(GameVariant.PACMAN, new PacManGameConfiguration_3D());
        ui.setGameConfiguration(GameVariant.PACMAN_XXL, new PacManGameXXLConfiguration_3D());
        for (GameVariant variant : GameVariant.values()) {
            ui.assets().addAll(ui.gameConfiguration(variant).assets());
        }
        ui.create(stage, initialSize());
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets: {}", ui.assets().summary(Map.of(
            Model3D.class,"3D models",
            Image.class, "images",
            Font.class, "fonts",
            Color.class, "colors",
            AudioClip.class, "audio clips")));
        ui.show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.stop();
    }
}