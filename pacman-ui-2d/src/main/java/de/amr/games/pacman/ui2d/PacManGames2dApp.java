/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacmanxxl.MapSelectionMode;
import de.amr.games.pacman.ui2d.scene.*;
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
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    public static final BooleanProperty PY_AUTOPILOT           = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Color> PY_CANVAS_COLOR  = new SimpleObjectProperty<>(Color.BLACK);
    public static final BooleanProperty PY_CANVAS_DECORATED    = new SimpleBooleanProperty(true);
    public static final ObjectProperty<MapSelectionMode> PY_MAP_SELECTION_MODE = new SimpleObjectProperty<>(MapSelectionMode.CUSTOM_MAPS_FIRST);
    public static final BooleanProperty PY_DEBUG_INFO          = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_IMMUNITY            = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_NIGHT_MODE          = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_HEIGHT          = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_Y);
    public static final BooleanProperty PY_PIP_ON              = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_OPACITY_PERCENT = new SimpleIntegerProperty(100);
    public static final IntegerProperty PY_SIMULATION_STEPS    = new SimpleIntegerProperty(1);

    private static Map<GameVariant, Map<GameSceneID, GameScene>> createGameScenes() {
        Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);
        for (GameVariant variant : GameVariant.values()) {
            switch (variant) {
                case MS_PACMAN, MS_PACMAN_TENGEN ->
                    gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                        GameSceneID.BOOT_SCENE,   new BootScene(),
                        GameSceneID.INTRO_SCENE,  new MsPacManIntroScene(),
                        GameSceneID.CREDIT_SCENE, new CreditScene(),
                        GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                        GameSceneID.CUT_SCENE_1,  new MsPacManCutScene1(),
                        GameSceneID.CUT_SCENE_2,  new MsPacManCutScene2(),
                        GameSceneID.CUT_SCENE_3,  new MsPacManCutScene3()
                    )));
                case PACMAN, PACMAN_XXL ->
                    gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                        GameSceneID.BOOT_SCENE,   new BootScene(),
                        GameSceneID.INTRO_SCENE,  new PacManIntroScene(),
                        GameSceneID.CREDIT_SCENE, new CreditScene(),
                        GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                        GameSceneID.CUT_SCENE_1,  new PacManCutScene1(),
                        GameSceneID.CUT_SCENE_2,  new PacManCutScene2(),
                        GameSceneID.CUT_SCENE_3,  new PacManCutScene3()
                    )));
                default -> throw new IllegalArgumentException("Unsupported game variant: " + variant);
            }
        }
        return gameScenesForVariant;
    }

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
        ui = new PacManGames2dUI(initialSize());
        ui.setGameScenes(createGameScenes());
        ui.createAndStart(stage);

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