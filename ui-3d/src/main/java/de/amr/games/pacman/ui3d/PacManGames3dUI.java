/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.Picker;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import de.amr.games.pacman.ui3d.model.Model3D;
import de.amr.games.pacman.ui3d.scene.Perspective;
import de.amr.games.pacman.ui3d.scene.PlayScene3D;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.games.pacman.ui2d.util.Ufx.toggle;

/**
 * User interface for the Pac-Man game variants (Pac-Man, Pac-Man XXL, Ms. Pac-Man) with a 3D
 * play scene ({@link PlayScene3D}). All others scenes are 2D only.
 * </p>
 * <p>The separation of the 2D-only UI into its own project was done to create a
 * <a href="https://github.com/armin-reichert/webfx-pacman">WebFX-version</a> of the game.
 * WebFX is a technology that transpiles a JavaFX application into a (very small) GWT application that runs inside
 * any browser supported by GWT. Unfortunately, WebFX has no support for JavaFX 3D so far.</p>
 *
 * @author Armin Reichert
 */
public class PacManGames3dUI extends PacManGames2dUI {

    static {
        System.setProperty("javafx.sg.warn", "true"); // WTF?
    }

    public static final BooleanProperty             PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode>    PY_3D_DRAW_MODE          = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(true);
    public static final BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.web("#333"));
    public static final StringProperty              PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty("knobs");
    public static final ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.GHOSTWHITE);
    public static final BooleanProperty             PY_3D_NIGHT_MODE         = new SimpleBooleanProperty(false);
    public static final BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE        = new SimpleObjectProperty<>(Perspective.FOLLOWING_PLAYER);
    public static final DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(3.5);
    public static final DoubleProperty              PY_3D_WALL_OPACITY       = new SimpleDoubleProperty(0.9);
    public static final ObjectProperty<Image>       PY_3D_WALLPAPER_DAY      = new SimpleObjectProperty<>();
    public static final ObjectProperty<Image>       PY_3D_WALLPAPER_NIGHT    = new SimpleObjectProperty<>();

    public static final String NO_TEXTURE = "No Texture";

    public static Picker<String> PICKER_LEVEL_COMPLETE;
    public static Picker<String> PICKER_GAME_OVER;

    @Override
    protected void loadAssets() {
        super.loadAssets();

        var messages = ResourceBundle.getBundle("de.amr.games.pacman.ui3d.texts.messages", getClass().getModule());
        bundles.add(messages);

        PICKER_LEVEL_COMPLETE = Picker.fromBundle(messages, "level.complete");
        PICKER_GAME_OVER      = Picker.fromBundle(messages, "game.over");

        ResourceManager rm = () -> PacManGames3dUI.class;

        theme.set("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        theme.set("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        theme.set("model3D.pellet", new Model3D(rm.url("model3D/12206_Fruit_v1_L3.obj")));

        theme.set("model3D.wallpaper.day",   rm.loadImage("graphics/sea-wallpaper.jpg"));
        theme.set("model3D.wallpaper.night", rm.loadImage("graphics/sea-wallpaper-night.jpg"));

        Map<String, PhongMaterial> texturesByName = new HashMap<>();
        theme.set("floorTextures", texturesByName);
        List.of("knobs", "plastic", "wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name)));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name)));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });

        theme.set("ghost.0.color.normal.dress",      theme.color("palette.red"));
        theme.set("ghost.0.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.0.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.1.color.normal.dress",      theme.color("palette.pink"));
        theme.set("ghost.1.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.1.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.2.color.normal.dress",      theme.color("palette.cyan"));
        theme.set("ghost.2.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.2.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.3.color.normal.dress",      theme.color("palette.orange"));
        theme.set("ghost.3.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.3.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.color.frightened.dress",    theme.color("palette.blue"));
        theme.set("ghost.color.frightened.eyeballs", theme.color("palette.rose"));
        theme.set("ghost.color.frightened.pupils",   theme.color("palette.rose"));

        theme.set("ghost.color.flashing.dress",      theme.color("palette.pale"));
        theme.set("ghost.color.flashing.eyeballs",   theme.color("palette.rose"));
        theme.set("ghost.color.flashing.pupils",     theme.color("palette.red"));

        theme.set("ms_pacman.color.head",            Color.rgb(255, 255, 0));
        theme.set("ms_pacman.color.palate",          Color.rgb(191, 79, 61));
        theme.set("ms_pacman.color.eyes",            Color.rgb(33, 33, 33));
        theme.set("ms_pacman.color.boobs",           Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        theme.set("ms_pacman.color.hairbow",         Color.rgb(255, 0, 0));
        theme.set("ms_pacman.color.hairbow.pearls",  Color.rgb(33, 33, 255));

        theme.set("pacman.color.head",               Color.rgb(255, 255, 0));
        theme.set("pacman.color.palate",             Color.rgb(191, 79, 61));
        theme.set("pacman.color.eyes",               Color.rgb(33, 33, 33));
    }

    @Override
    protected void logAssets() {
        Logger.info("Assets loaded: {}", theme.summary(List.of(
            new Pair<>(Model3D.class,"3D models"),
            new Pair<>(Image.class, "images"),
            new Pair<>(Font.class, "fonts"),
            new Pair<>(Color.class, "colors"),
            new Pair<>(AudioClip.class, "audio clips")
        )));
    }

    @Override
    protected Dimension2D computeMainSceneSize(Rectangle2D screenSize) {
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight(), width = aspect * height;
        return new Dimension2D(width, height);
    }

    @Override
    protected void createGameScenes() {
        super.createGameScenes();
        for (var variant : gameController().supportedVariants()) {
            var playScene3D = new PlayScene3D();
            playScene3D.setContext(this);
            playScene3D.setParentScene(mainScene);
            gameSceneManager.putGameScene(playScene3D, variant, GameSceneID.PLAY_SCENE_3D);
            Logger.info("Added 3D play scene for variant " + variant);
        }
    }

    @Override
    protected void createGamePage() {
        PY_3D_WALLPAPER_DAY.set(theme.get("model3D.wallpaper.day"));
        PY_3D_WALLPAPER_NIGHT.set(theme.get("model3D.wallpaper.night"));
        gamePage = new GamePage3D(this, mainScene);
        gamePage.dashboard().addInfoBox(3, tt("infobox.3D_settings.title"), new InfoBox3D());
        gamePage.canvasPane().decoratedCanvas().decoratedPy.bind(PY_CANVAS_DECORATED);
        gamePage.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
                    return Ufx.coloredBackground(Color.rgb(100,100,200));
                }
                return Ufx.wallpaperBackground(PY_3D_NIGHT_MODE.get() ? PY_3D_WALLPAPER_NIGHT.get() : PY_3D_WALLPAPER_DAY.get());
            },
            PY_3D_DRAW_MODE, PY_3D_NIGHT_MODE, PY_3D_WALLPAPER_DAY, PY_3D_WALLPAPER_NIGHT
        ));
        gameScenePy.addListener((py, ov, newGameScene) -> {
            gamePage.hideContextMenu();
            if (isGameSceneRegisteredAs(newGameScene, GameSceneID.PLAY_SCENE_3D)) {
                gamePage.embedGameScene3D(newGameScene);
            } else if (newGameScene instanceof GameScene2D scene2D) {
                gamePage.embedGameScene2D(scene2D);
            } else {
                Logger.warn("Cannot embed game scene {}", newGameScene);
            }
        });
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        super.onLevelStarted(event);
        LocalTime now = LocalTime.now();
        PY_3D_NIGHT_MODE.set(now.getHour() >= 21 || now.getHour() <= 5);
    }

    @Override
    protected StringBinding stageTitleBinding() {
        return Bindings.createStringBinding(() -> {
            var tk = "app.title." + gameVariantPy.get().resourceKey() + (clock.pausedPy.get() ? ".paused" : "");
            var dim = tt(PY_3D_ENABLED.get() ? "threeD" : "twoD");
            return tt(tk, dim);
        }, clock.pausedPy, gameVariantPy, PY_3D_ENABLED);
    }

    @Override
    protected GameScene gameSceneForCurrentGameState() {
        GameScene gameScene = super.gameSceneForCurrentGameState();
        if (PY_3D_ENABLED.get() && isGameSceneRegisteredAs(gameScene, GameSceneID.PLAY_SCENE)) {
            GameScene playScene3D = gameSceneManager.gameScene(game().variant(), GameSceneID.PLAY_SCENE_3D);
            return playScene3D != null ? playScene3D : gameScene;
        }
        return gameScene;
    }

    @Override
    public void selectNextPerspective() {
        var next = Perspective.next(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(next);
        showFlashMessage(tt("camera_perspective", tt(next.name())));
    }

    @Override
    public void selectPrevPerspective() {
        var prev = Perspective.previous(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(prev);
        showFlashMessage(tt("camera_perspective", tt(prev.name())));
    }

    @Override
    public void toggle2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            if (isGameSceneRegisteredAs(gameScene, GameSceneID.PLAY_SCENE)
                || isGameSceneRegisteredAs(gameScene, GameSceneID.PLAY_SCENE_3D)) {
                updateGameScene(true);
                gameScenePy.get().onSceneVariantSwitch(gameScene);
            }
            gameController().update();
            if (!game().isPlaying()) {
                showFlashMessage(tt(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }

    @Override
    public void toggleDrawMode() {
        PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
    }
}