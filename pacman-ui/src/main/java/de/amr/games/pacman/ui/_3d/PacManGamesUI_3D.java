/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.beans.binding.Bindings;

import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;

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
        assets().addAssets3D();
    }

    @Override
    protected void createGameView() {
        gameView = new GameView3D(this);
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
            clock().pausedProperty(), gameScenePy, gameView.node().heightProperty(), PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE)
        );
    }
}