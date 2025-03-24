/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.uilib.Picker;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.model3D.Model3D;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.UIGlobals.*;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;
import static de.amr.games.pacman.uilib.Ufx.toggle;

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

    public PacManGamesUI_3D() {
        super(); // loads 2D assets!
        THE_GAME_CONTEXT = this;
        loadAssets3D();
    }

    private void loadAssets3D() {
        ResourceManager rm = this::getClass;
        THE_ASSETS.addBundle(rm.getModuleBundle("de.amr.games.pacman.ui.texts.messages3d"));

        ResourceManager uiLibResources = () -> Ufx.class;
        THE_ASSETS.store("model3D.pacman", new Model3D(uiLibResources.url("model3D/pacman.obj")));
        THE_ASSETS.store("model3D.pellet", new Model3D(uiLibResources.url("model3D/fruit.obj")));

        Model3D ghostModel3D = new Model3D(uiLibResources.url("model3D/ghost.obj"));
        THE_ASSETS.store("model3D.ghost",               ghostModel3D);
        THE_ASSETS.store("model3D.ghost.mesh.dress",    ghostModel3D.mesh("Sphere.004_Sphere.034_light_blue_ghost"));
        THE_ASSETS.store("model3D.ghost.mesh.pupils",   ghostModel3D.mesh("Sphere.010_Sphere.039_grey_wall"));
        THE_ASSETS.store("model3D.ghost.mesh.eyeballs", ghostModel3D.mesh("Sphere.009_Sphere.036_white"));

        pickerForGameOverTexts = Picker.fromBundle(THE_ASSETS.bundles().getLast(), "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(THE_ASSETS.bundles().getLast(), "level.complete");
    }

    @Override
    protected void createGameView(Scene parentScene) {
        gameView = new GameView3D(parentScene);
        gameView.gameSceneProperty().bind(gameScenePy);
    }

    @Override
    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                String sceneName = currentGameScene().map(gameScene -> gameScene.getClass().getSimpleName()).orElse(null);
                String sceneNameText = sceneName != null && PY_DEBUG_INFO_VISIBLE.get() ? " [%s]".formatted(sceneName) : "";
                String assetNamespace = gameConfiguration().assetNamespace();
                String key = "app.title." + assetNamespace;
                if (THE_CLOCK.isPaused()) {
                    key += ".paused";
                }
                String modeKey = locText(PY_3D_ENABLED.get() ? "threeD" : "twoD");
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return locText(key, modeKey) + sceneNameText + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return locText(key, modeKey) + sceneNameText;
            },
            THE_CLOCK.pausedProperty(), gameVariantPy, gameScenePy, gameView.heightProperty(), PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE)
        );
    }

    @Override
    public void togglePlayScene2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            if (currentGameSceneHasID("PlayScene2D") || currentGameSceneHasID("PlayScene3D")) {
                updateGameScene(true);
                THE_GAME_CONTROLLER.update(); //TODO needed?
            }
            if (!THE_GAME_CONTROLLER.game().isPlaying()) {
                showFlashMessage(locText(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }
}