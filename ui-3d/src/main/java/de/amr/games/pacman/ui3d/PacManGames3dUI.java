/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static de.amr.games.pacman.ui3d.GameParameters3D.*;

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

    @Override
    public void loadAssets(ResourceManager rm, boolean log) {
        // Load assets for 2D UI from other module
        super.loadAssets(() -> PacManGames2dUI.class, false);

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

        assets.set("pacman.color.head",               Color.rgb(255, 255, 0));
        assets.set("pacman.color.eyes",               Color.rgb(33, 33, 33));
        assets.set("pacman.color.palate",             Color.rgb(240, 180, 160));

        if (log) {
            Logger.info("Assets loaded: {}", assets.summary(List.of(
                new Pair<>(Model3D.class,"3D models"),
                new Pair<>(Image.class, "images"),
                new Pair<>(Font.class, "fonts"),
                new Pair<>(Color.class, "colors"),
                new Pair<>(AudioClip.class, "audio clips")
            )));
        }
    }

    @Override
    protected GamePage3D createGamePage(Scene parentScene) {
        var gamePage = new GamePage3D(this, parentScene);
        gamePage.sign(assets.font("font.monospaced", 9), locText("app.signature"));
        return gamePage;
    }

    @Override
    protected ObservableValue<String> stageTitleBinding() {
        return Bindings.createStringBinding(() -> {
            // resource key is composed from game variant, paused state and display mode (2D, 3D)
            String gameVariantPart = "app.title." + gameVariantPy.get().resourceKey();
            String pausedPart = clock.pausedPy.get() ? ".paused" : "";
            String displayMode = locText(PY_3D_ENABLED.get() ? "threeD" : "twoD");
            return locText(gameVariantPart + pausedPart, displayMode);
        }, clock.pausedPy, gameVariantPy, PY_3D_ENABLED);
    }

    @Override
    protected GameScene gameSceneForCurrentGameState() {
        GameScene gameScene = super.gameSceneForCurrentGameState();
        if (PY_3D_ENABLED.get() && hasID(gameScene, GameSceneID.PLAY_SCENE)) {
            GameScene playScene3D = gameScene(game().variant(), GameSceneID.PLAY_SCENE_3D);
            return playScene3D != null ? playScene3D : gameScene;
        }
        return gameScene;
    }

    @Override
    public void selectNextPerspective() {
        var next = Perspective.next(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(next);
        showFlashMessage(locText("camera_perspective", locText(next.name())));
    }

    @Override
    public void selectPrevPerspective() {
        var prev = Perspective.previous(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(prev);
        showFlashMessage(locText("camera_perspective", locText(prev.name())));
    }

    @Override
    public void toggle2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            if (hasID(gameScene, GameSceneID.PLAY_SCENE)
                || hasID(gameScene, GameSceneID.PLAY_SCENE_3D)) {
                updateGameScene(true);
                gameScenePy.get().onSceneVariantSwitch(gameScene);
            }
            gameController().update();
            if (!game().isPlaying()) {
                showFlashMessage(locText(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }

    @Override
    public void toggleDrawMode() {
        PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
    }
}