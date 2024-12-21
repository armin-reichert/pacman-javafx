/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.lib.Picker;
import de.amr.games.pacman.ui2d.lib.Ufx;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui2d.lib.Ufx.toggle;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_ENABLED;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_FLOOR_COLOR;

/**
 * User interface for all Pac-Man game variants with a 3D play scene. All others scenes are in 2D.
 * </p>
 * <p>The separation of the 2D-only UI into its own project originally was done to create a
 * <a href="https://github.com/armin-reichert/webfx-pacman">WebFX-version</a> of the game.
 * WebFX is a technology that transpiles a JavaFX application into a (very small) GWT application that runs inside
 * any browser supported by GWT. Unfortunately, WebFX has no support for JavaFX 3D so far.</p>
 *
 * @author Armin Reichert
 */
public class PacManGamesUI_3D extends PacManGamesUI {

    public void loadAssets() {
        super.loadAssets();
        ResourceManager rm = this::getClass;
        assets.addBundle(rm.getModuleBundle("de.amr.games.pacman.ui3d.texts.messages"));

        assets.store("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        assets.store("model3D.pellet", new Model3D(rm.url("model3D/fruit.obj")));

        Model3D ghostModel3D = new Model3D(rm.url("model3D/ghost.obj"));
        assets.store("model3D.ghost",               ghostModel3D);
        assets.store("model3D.ghost.mesh.dress",    ghostModel3D.mesh("Sphere.004_Sphere.034_light_blue_ghost"));
        assets.store("model3D.ghost.mesh.pupils",   ghostModel3D.mesh("Sphere.010_Sphere.039_grey_wall"));
        assets.store("model3D.ghost.mesh.eyeballs", ghostModel3D.mesh("Sphere.009_Sphere.036_white"));

        assets.store("wallpaper.day",   Ufx.wallpaperBackground(rm.loadImage("graphics/abstract-wallpaper-1.jpg")));
        assets.store("wallpaper.night", Ufx.wallpaperBackground(rm.loadImage("graphics/abstract-wallpaper-2.jpg")));

        Map<String, PhongMaterial> texturesByName = new LinkedHashMap<>();
        Stream.of("Carpet", "Rubber", "Wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name.toLowerCase())));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name.toLowerCase())));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });
        assets.store("floor_textures", texturesByName);

        // Ms. Pac-Man
        assets.store("ms_pacman.pac.color.head",                  Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman.pac.color.boobs",                 Color.valueOf(Arcade.Palette.YELLOW).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.pac.color.hairbow",               Color.valueOf(Arcade.Palette.RED));
        assets.store("ms_pacman.pac.color.hairbow.pearls",        Color.valueOf(Arcade.Palette.BLUE));

        assets.store("ms_pacman.ghost.0.color.normal.dress",      Color.valueOf(Arcade.Palette.RED));
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.0.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.1.color.normal.dress",      Color.valueOf(Arcade.Palette.PINK));
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.1.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.2.color.normal.dress",      Color.valueOf(Arcade.Palette.CYAN));
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.2.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.3.color.normal.dress",      Color.valueOf(Arcade.Palette.ORANGE));
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.3.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.color.frightened.dress",    Color.valueOf(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", Color.valueOf(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.frightened.pupils",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.flashing.dress",      Color.valueOf(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.color.flashing.eyeballs",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.flashing.pupils",     Color.valueOf(Arcade.Palette.RED));

        // Tengen Ms. Pac-Man: see GameAssets2D

        // Pac-Man
        assets.store("pacman.pac.color.head",                  Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman.ghost.0.color.normal.dress",      Color.valueOf(Arcade.Palette.RED));
        assets.store("pacman.ghost.0.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.0.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.1.color.normal.dress",      Color.valueOf(Arcade.Palette.PINK));
        assets.store("pacman.ghost.1.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.1.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.2.color.normal.dress",      Color.valueOf(Arcade.Palette.CYAN));
        assets.store("pacman.ghost.2.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.2.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.3.color.normal.dress",      Color.valueOf(Arcade.Palette.ORANGE));
        assets.store("pacman.ghost.3.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.3.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.color.frightened.dress",    Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.color.frightened.eyeballs", Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.frightened.pupils",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.flashing.dress",      Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.color.flashing.eyeballs",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.flashing.pupils",     Color.valueOf(Arcade.Palette.RED));

        // Pac-Man XXL
        assets.store("pacman_xxl.pac.color.head",                  Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman_xxl.ghost.0.color.normal.dress",      Color.valueOf(Arcade.Palette.RED));
        assets.store("pacman_xxl.ghost.0.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.0.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.1.color.normal.dress",      Color.valueOf(Arcade.Palette.PINK));
        assets.store("pacman_xxl.ghost.1.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.1.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.2.color.normal.dress",      Color.valueOf(Arcade.Palette.CYAN));
        assets.store("pacman_xxl.ghost.2.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.2.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.3.color.normal.dress",      Color.valueOf(Arcade.Palette.ORANGE));
        assets.store("pacman_xxl.ghost.3.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.3.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.color.frightened.dress",    Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman_xxl.ghost.color.frightened.eyeballs", Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman_xxl.ghost.color.frightened.pupils",   Color.valueOf(Arcade.Palette.ROSE));

        assets.store("pacman_xxl.ghost.color.flashing.dress",      Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.color.flashing.eyeballs",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman_xxl.ghost.color.flashing.pupils",     Color.valueOf(Arcade.Palette.RED));

        pickerGameOver = Picker.fromBundle(assets.bundles().getLast(), "game.over");
        pickerLevelComplete = Picker.fromBundle(assets.bundles().getLast(), "level.complete");
        sound().setAssets(assets);
    }

    @Override
    protected void createGamePage(Scene parentScene) {
        gamePage = new GamePage3D(this, parentScene);
        gamePage.setActionOpenEditor(actionOpenEditor);
        gamePage.gameScenePy.bind(gameScenePy);
    }

    @Override
    protected StringBinding stageTitleBinding() {
        return Bindings.createStringBinding(() -> {
            String sceneName = currentGameScene().map(gameScene -> gameScene.getClass().getSimpleName()).orElse(null);
            String sceneNameText = sceneName != null && PY_DEBUG_INFO_VISIBLE.get() ? " [%s]".formatted(sceneName) : "";
            String assetKeyPrefix = currentGameConfig().assetKeyPrefix();
            // resource key is composed of game variant, paused state and display mode (2D, 3D)
            String key = "app.title." + assetKeyPrefix;
            if (clock.isPaused()) {
                key += ".paused";
            }
            String modeKey = locText(PY_3D_ENABLED.get() ? "threeD" : "twoD");
            if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                return locText(key, modeKey) + sceneNameText + " (%.2fx)".formatted(gameScene2D.scaling());
            }
            return locText(key, modeKey) + sceneNameText;
        },
        clock.pausedPy, gameVariantPy, gameScenePy, gamePage.heightProperty(),
        PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE);
    }

    @Override
    public void togglePlayScene2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            if (currentGameSceneHasID("PlayScene2D") || currentGameSceneHasID("PlayScene3D")) {
                updateGameScene(true);
                gameController().update(); //TODO needed anymore?
            }
            if (!game().isPlaying()) {
                showFlashMessage(locText(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }
}