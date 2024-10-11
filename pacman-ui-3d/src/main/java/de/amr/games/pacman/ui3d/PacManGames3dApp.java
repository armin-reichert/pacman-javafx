/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.BootScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.*;
import de.amr.games.pacman.ui2d.scene.pacman.*;
import de.amr.games.pacman.ui2d.scene.tengen.TengenIntroScene;
import de.amr.games.pacman.ui2d.scene.tengen.TengenStartScene;
import de.amr.games.pacman.ui3d.model.Model3D;
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
    public static final StringProperty              PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty(GameAssets3D.NO_TEXTURE);
    public static final ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.GHOSTWHITE);
    public static final BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Perspective.Name> PY_3D_PERSPECTIVE   = new SimpleObjectProperty<>(Perspective.Name.TOTAL);
    public static final DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    public static final DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(0.9);

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        return new Dimension2D(width, height);
    }

    private PacManGames3dUI ui;

    @Override
    public void init() {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        GameController.create(userDir);
        GameController.it().selectGame(GameVariant.PACMAN);
        PY_3D_ENABLED.set(false);
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames3dUI();
        ui.loadAssets();
        ui.setGameScenes(GameVariant.MS_PACMAN, Map.of(
            GameSceneID.BOOT_SCENE,    new BootScene(),
            GameSceneID.INTRO_SCENE,   new MsPacManIntroScene(),
            GameSceneID.START_SCENE,  new MsPacManStartScene(),
            GameSceneID.PLAY_SCENE,    new PlayScene2D(),
            GameSceneID.PLAY_SCENE_3D, new PlayScene3D(),
            GameSceneID.CUT_SCENE_1,   new MsPacManCutScene1(),
            GameSceneID.CUT_SCENE_2,   new MsPacManCutScene2(),
            GameSceneID.CUT_SCENE_3,   new MsPacManCutScene3()
        ));
        ui.setGameScenes(GameVariant.MS_PACMAN_TENGEN, Map.of(
            GameSceneID.BOOT_SCENE,    new BootScene(),
            GameSceneID.INTRO_SCENE,   new TengenIntroScene(),
            GameSceneID.START_SCENE,  new TengenStartScene(),
            GameSceneID.PLAY_SCENE,    new PlayScene2D(),
            GameSceneID.PLAY_SCENE_3D, new PlayScene3D(),
            GameSceneID.CUT_SCENE_1,   new MsPacManCutScene1(),
            GameSceneID.CUT_SCENE_2,   new MsPacManCutScene2(),
            GameSceneID.CUT_SCENE_3,   new MsPacManCutScene3()
        ));
        ui.setGameScenes(GameVariant.PACMAN, Map.of(
            GameSceneID.BOOT_SCENE,    new BootScene(),
            GameSceneID.INTRO_SCENE,   new PacManIntroScene(),
            GameSceneID.START_SCENE,  new PacManStartScene(),
            GameSceneID.PLAY_SCENE,    new PlayScene2D(),
            GameSceneID.PLAY_SCENE_3D, new PlayScene3D(),
            GameSceneID.CUT_SCENE_1,   new PacManCutScene1(),
            GameSceneID.CUT_SCENE_2,   new PacManCutScene2(),
            GameSceneID.CUT_SCENE_3,   new PacManCutScene3()
        ));
        ui.setGameScenes(GameVariant.PACMAN_XXL, Map.of(
            GameSceneID.BOOT_SCENE,    new BootScene(),
            GameSceneID.INTRO_SCENE,   new PacManIntroScene(),
            GameSceneID.START_SCENE,  new PacManStartScene(),
            GameSceneID.PLAY_SCENE,    new PlayScene2D(),
            GameSceneID.PLAY_SCENE_3D, new PlayScene3D(),
            GameSceneID.CUT_SCENE_1,   new PacManCutScene1(),
            GameSceneID.CUT_SCENE_2,   new PacManCutScene2(),
            GameSceneID.CUT_SCENE_3,   new PacManCutScene3()
        ));
        ui.createAndStart(stage, initialSize());

        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets: {}", ui.assets().summary(Map.of(
            Model3D.class,"3D models",
            Image.class, "images",
            Font.class, "fonts",
            Color.class, "colors",
            AudioClip.class, "audio clips")));
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.stop();
    }
}