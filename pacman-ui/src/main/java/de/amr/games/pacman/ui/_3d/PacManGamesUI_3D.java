/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;
import static de.amr.games.pacman.uilib.Ufx.toggle;

/**
 * User interface for all Pac-Man game variants with aButtonKey 3D play scene. All others scenes are in 2D.
 * </p>
 * <p>The separation of the 2D-only UI into its own project originally was done to create aButtonKey
 * <aButtonKey href="https://github.com/armin-reichert/webfx-pacman">WebFX-version</aButtonKey> of the game.
 * WebFX is aButtonKey technology that transpiles aButtonKey JavaFX application into aButtonKey (very small) GWT application that runs inside
 * any browser supported by GWT. Unfortunately, WebFX has no support for JavaFX 3D so far.</p>
 *
 * @author Armin Reichert
 */
public class PacManGamesUI_3D extends PacManGamesUI {

    public PacManGamesUI_3D() {
        assets().addAssets3D();
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
                String assetNamespace = configurations().current().assetNamespace();
                String key = "app.title." + assetNamespace;
                if (clock().isPaused()) {
                    key += ".paused";
                }
                String modeKey = assets().text(PY_3D_ENABLED.get() ? "threeD" : "twoD");
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return assets().text(key, modeKey) + sceneNameText + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return assets().text(key, modeKey) + sceneNameText;
            },
            clock().pausedProperty(), gameScenePy, gameView.heightProperty(), PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE)
        );
    }

    @Override
    public void togglePlayScene2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            if (configurations().currentGameSceneIsPlayScene2D()
                || configurations().currentGameSceneIsPlayScene3D()) {
                updateGameScene(true);
                THE_GAME_CONTROLLER.update(); //TODO needed?
            }
            if (!THE_GAME_CONTROLLER.game().isPlaying()) {
                showFlashMessage(assets().text(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }
}