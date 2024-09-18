/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.PacManGames2dApp;
import de.amr.games.pacman.ui2d.scene.*;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    public static final String NO_TEXTURE = "No Texture";

    public static final BooleanProperty PY_3D_AXES_VISIBLE                   = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE             = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(false);
    public static final BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.web("#202020"));
    public static final StringProperty PY_3D_FLOOR_TEXTURE                   = new SimpleStringProperty(NO_TEXTURE);
    public static final ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.GHOSTWHITE);
    public static final BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE        = new SimpleObjectProperty<>(Perspective.FOLLOWING_PLAYER);
    public static final DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    public static final DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(0.9);

    private static Dimension2D computeInitialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        return new Dimension2D(width, height);
    }

    private static void addAssets(AssetStorage assets) {
        // Load assets for 2D UI from other module
        PacManGames2dApp.addAssets(assets);

        ResourceManager rm = () -> PacManGames3dApp.class;

        assets.addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui3d.texts.messages", rm.rootClass().getModule()));

        assets.store("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        assets.store("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        assets.store("model3D.pellet", new Model3D(rm.url("model3D/fruit.obj")));

        assets.store("wallpaper.day",   rm.loadImage("graphics/sea-wallpaper.jpg"));
        assets.store("wallpaper.night", rm.loadImage("graphics/sea-wallpaper-night.jpg"));

        Map<String, PhongMaterial> texturesByName = new LinkedHashMap<>();
        assets.store("floorTextures", texturesByName);
        Stream.of("Carpet", "Rubber", "Wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name.toLowerCase())));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name.toLowerCase())));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });

        assets.store("ghost.0.color.normal.dress",      assets.color("palette.red"));
        assets.store("ghost.0.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ghost.0.color.normal.pupils",     assets.color("palette.blue"));

        assets.store("ghost.1.color.normal.dress",      assets.color("palette.pink"));
        assets.store("ghost.1.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ghost.1.color.normal.pupils",     assets.color("palette.blue"));

        assets.store("ghost.2.color.normal.dress",      assets.color("palette.cyan"));
        assets.store("ghost.2.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ghost.2.color.normal.pupils",     assets.color("palette.blue"));

        assets.store("ghost.3.color.normal.dress",      assets.color("palette.orange"));
        assets.store("ghost.3.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ghost.3.color.normal.pupils",     assets.color("palette.blue"));

        assets.store("ghost.color.frightened.dress",    assets.color("palette.blue"));
        assets.store("ghost.color.frightened.eyeballs", assets.color("palette.rose"));
        assets.store("ghost.color.frightened.pupils",   assets.color("palette.rose"));

        assets.store("ghost.color.flashing.dress",      assets.color("palette.pale"));
        assets.store("ghost.color.flashing.eyeballs",   assets.color("palette.rose"));
        assets.store("ghost.color.flashing.pupils",     assets.color("palette.red"));

        assets.store("ms_pacman.color.head",            Color.rgb(255, 255, 0));
        assets.store("ms_pacman.color.eyes",            Color.rgb(33, 33, 33));
        assets.store("ms_pacman.color.palate",          Color.rgb(240, 180, 160));
        assets.store("ms_pacman.color.boobs",           Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.color.hairbow",         Color.rgb(255, 0, 0));
        assets.store("ms_pacman.color.hairbow.pearls",  Color.rgb(33, 33, 255));

        assets.store("tengen.color.head",               Color.rgb(255, 255, 0));
        assets.store("tengen.color.eyes",               Color.rgb(33, 33, 33));
        assets.store("tengen.color.palate",             Color.rgb(240, 180, 160));
        assets.store("tengen.color.boobs",              Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("tengen.color.hairbow",            Color.rgb(255, 0, 0));
        assets.store("tengen.color.hairbow.pearls",     Color.rgb(33, 33, 255));

        assets.store("pacman.color.head",               Color.rgb(255, 255, 0));
        assets.store("pacman.color.eyes",               Color.rgb(33, 33, 33));
        assets.store("pacman.color.palate",             Color.rgb(240, 180, 160));

        assets.store("pacman_xxl.color.head",           Color.rgb(255, 255, 0));
        assets.store("pacman_xxl.color.eyes",           Color.rgb(33, 33, 33));
        assets.store("pacman_xxl.color.palate",         Color.rgb(240, 180, 160));

        GameSounds.setAssets(assets);
    }

    private static Map<GameVariant, Map<GameSceneID, GameScene>> createGameScenes() {
        Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);
        for (var variant : GameVariant.values()) {
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
            }
            gameScenesForVariant.get(variant).put(GameSceneID.PLAY_SCENE_3D, new PlayScene3D());
            Logger.info("Added 3D play scene for variant " + variant);
        }
        return gameScenesForVariant;
    }

    private final GameClockFX clock = new GameClockFX();

    @Override
    public void init() {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        GameController.create(userDir);
        GameController.it().selectGame(GameVariant.PACMAN);
        PY_3D_ENABLED.set(false);
    }

    @Override
    public void start(Stage stage) {
        var ui = new PacManGames3dUI(computeInitialSize());
        addAssets(ui.assets());
        ui.create(stage, clock);
        ui.setGameScenes(createGameScenes());
        ui.start();

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
        clock.stop();
    }
}