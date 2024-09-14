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
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_ENABLED;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_FLOOR_COLOR;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private static void addAssets(AssetMap assets) {
        // Load assets for 2D UI from other module
        PacManGames2dApp.addAssets(assets);

        ResourceManager rm = () -> PacManGames3dApp.class;

        assets.addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui3d.texts.messages", rm.rootClass().getModule()));

        assets.set("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        assets.set("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        assets.set("model3D.pellet", new Model3D(rm.url("model3D/fruit.obj")));

        assets.set("wallpaper.day",   rm.loadImage("graphics/sea-wallpaper.jpg"));
        assets.set("wallpaper.night", rm.loadImage("graphics/sea-wallpaper-night.jpg"));

        Map<String, PhongMaterial> texturesByName = new LinkedHashMap<>();
        assets.set("floorTextures", texturesByName);
        Stream.of("Carpet", "Rubber", "Wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name.toLowerCase())));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name.toLowerCase())));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });

        assets.set("ghost.0.color.normal.dress",      assets.color("palette.red"));
        assets.set("ghost.0.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.set("ghost.0.color.normal.pupils",     assets.color("palette.blue"));

        assets.set("ghost.1.color.normal.dress",      assets.color("palette.pink"));
        assets.set("ghost.1.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.set("ghost.1.color.normal.pupils",     assets.color("palette.blue"));

        assets.set("ghost.2.color.normal.dress",      assets.color("palette.cyan"));
        assets.set("ghost.2.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.set("ghost.2.color.normal.pupils",     assets.color("palette.blue"));

        assets.set("ghost.3.color.normal.dress",      assets.color("palette.orange"));
        assets.set("ghost.3.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.set("ghost.3.color.normal.pupils",     assets.color("palette.blue"));

        assets.set("ghost.color.frightened.dress",    assets.color("palette.blue"));
        assets.set("ghost.color.frightened.eyeballs", assets.color("palette.rose"));
        assets.set("ghost.color.frightened.pupils",   assets.color("palette.rose"));

        assets.set("ghost.color.flashing.dress",      assets.color("palette.pale"));
        assets.set("ghost.color.flashing.eyeballs",   assets.color("palette.rose"));
        assets.set("ghost.color.flashing.pupils",     assets.color("palette.red"));

        assets.set("ms_pacman.color.head",            Color.rgb(255, 255, 0));
        assets.set("ms_pacman.color.eyes",            Color.rgb(33, 33, 33));
        assets.set("ms_pacman.color.palate",          Color.rgb(240, 180, 160));
        assets.set("ms_pacman.color.boobs",           Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        assets.set("ms_pacman.color.hairbow",         Color.rgb(255, 0, 0));
        assets.set("ms_pacman.color.hairbow.pearls",  Color.rgb(33, 33, 255));

        assets.set("tengen.color.head",            Color.rgb(255, 255, 0));
        assets.set("tengen.color.eyes",            Color.rgb(33, 33, 33));
        assets.set("tengen.color.palate",          Color.rgb(240, 180, 160));
        assets.set("tengen.color.boobs",           Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        assets.set("tengen.color.hairbow",         Color.rgb(255, 0, 0));
        assets.set("tengen.color.hairbow.pearls",  Color.rgb(33, 33, 255));

        assets.set("pacman.color.head",               Color.rgb(255, 255, 0));
        assets.set("pacman.color.eyes",               Color.rgb(33, 33, 33));
        assets.set("pacman.color.palate",             Color.rgb(240, 180, 160));

        assets.set("pacman_xxl.color.head",               Color.rgb(255, 255, 0));
        assets.set("pacman_xxl.color.eyes",               Color.rgb(33, 33, 33));
        assets.set("pacman_xxl.color.palate",             Color.rgb(240, 180, 160));

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
        var ui = new PacManGames3dUI(clock, createGameScenes());
        addAssets(ui.assets());
        ui.create(stage, computeSize());
        ui.start();

        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets: {}", ui.assets().summary(List.of(
            new Pair<>(Model3D.class,"3D models"),
            new Pair<>(Image.class, "images"),
            new Pair<>(Font.class, "fonts"),
            new Pair<>(Color.class, "colors"),
            new Pair<>(AudioClip.class, "audio clips")
        )));
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        clock.stop();
    }

    private Dimension2D computeSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        return new Dimension2D(width, height);
    }
}