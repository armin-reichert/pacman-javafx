/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacmanxxl.MapSelectionMode;
import de.amr.games.pacman.ui2d.scene.BootScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.*;
import de.amr.games.pacman.ui2d.scene.pacman.PacManCutScene1;
import de.amr.games.pacman.ui2d.scene.pacman.PacManCutScene2;
import de.amr.games.pacman.ui2d.scene.pacman.PacManCutScene3;
import de.amr.games.pacman.ui2d.scene.pacman.PacManIntroScene;
import de.amr.games.pacman.ui2d.scene.tengen.TengenIntroScene;
import de.amr.games.pacman.ui2d.scene.tengen.TengenSettingsScene;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    public static final BooleanProperty PY_AUTOPILOT                  = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Color> PY_CANVAS_BG_COLOR      = new SimpleObjectProperty<>(Color.BLACK);
    public static final BooleanProperty PY_GAME_CANVAS_HAS_BORDER     = new SimpleBooleanProperty(true);
    public static final BooleanProperty PY_GAME_CANVAS_CORNERS_ROUNDED = new SimpleBooleanProperty(true);
    public static final BooleanProperty PY_GAME_CANVAS_HAS_DECORATION = new SimpleBooleanProperty(true);
    public static final ObjectProperty<MapSelectionMode> PY_MAP_SELECTION_MODE = new SimpleObjectProperty<>(MapSelectionMode.CUSTOM_MAPS_FIRST);
    public static final BooleanProperty PY_DEBUG_INFO                 = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_IMMUNITY                   = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_NIGHT_MODE                 = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_HEIGHT                 = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_Y);
    public static final BooleanProperty PY_PIP_ON                     = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_OPACITY_PERCENT        = new SimpleIntegerProperty(100);
    public static final IntegerProperty PY_SIMULATION_STEPS           = new SimpleIntegerProperty(1);

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }

    private PacManGames2dUI ui;

    @Override
    public void init() {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        GameController.create(userDir);
        GameController.it().selectGame(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGames2dUI();
        ui.loadAssets();
        ui.setGameScenes(GameVariant.MS_PACMAN, Map.of(
            GameSceneID.BOOT_SCENE,   new BootScene(),
            GameSceneID.INTRO_SCENE,  new MsPacManIntroScene(),
            GameSceneID.CREDIT_SCENE, new MsPacManCreditScene(),
            GameSceneID.PLAY_SCENE,   new PlayScene2D(),
            GameSceneID.CUT_SCENE_1,  new MsPacManCutScene1(),
            GameSceneID.CUT_SCENE_2,  new MsPacManCutScene2(),
            GameSceneID.CUT_SCENE_3,  new MsPacManCutScene3()
        ));
        ui.setGameScenes(GameVariant.MS_PACMAN_TENGEN, Map.of(
            GameSceneID.BOOT_SCENE,   new BootScene(),
            GameSceneID.INTRO_SCENE,  new TengenIntroScene(),
            GameSceneID.CREDIT_SCENE, new TengenSettingsScene(),
            GameSceneID.PLAY_SCENE,   new PlayScene2D(),
            GameSceneID.CUT_SCENE_1,  new MsPacManCutScene1(),
            GameSceneID.CUT_SCENE_2,  new MsPacManCutScene2(),
            GameSceneID.CUT_SCENE_3,  new MsPacManCutScene3()
        ));
        ui.setGameScenes(GameVariant.PACMAN, Map.of(
            GameSceneID.BOOT_SCENE,   new BootScene(),
            GameSceneID.INTRO_SCENE,  new PacManIntroScene(),
            GameSceneID.CREDIT_SCENE, new MsPacManCreditScene(),
            GameSceneID.PLAY_SCENE,   new PlayScene2D(),
            GameSceneID.CUT_SCENE_1,  new PacManCutScene1(),
            GameSceneID.CUT_SCENE_2,  new PacManCutScene2(),
            GameSceneID.CUT_SCENE_3,  new PacManCutScene3()
        ));
        ui.setGameScenes(GameVariant.PACMAN_XXL, Map.of(
            GameSceneID.BOOT_SCENE,   new BootScene(),
            GameSceneID.INTRO_SCENE,  new PacManIntroScene(),
            GameSceneID.CREDIT_SCENE, new MsPacManCreditScene(),
            GameSceneID.PLAY_SCENE,   new PlayScene2D(),
            GameSceneID.CUT_SCENE_1,  new PacManCutScene1(),
            GameSceneID.CUT_SCENE_2,  new PacManCutScene2(),
            GameSceneID.CUT_SCENE_3,  new PacManCutScene3()
        ));
        ui.createAndStart(stage, initialSize());

        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets loaded: {}", ui.assets().summary(
            Map.of(Image.class, "images",  Font.class, "fonts", Color.class, "colors", AudioClip.class, "audio clips")
        ));
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.stop();
    }
}