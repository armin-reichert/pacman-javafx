/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.Picker;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.Scene;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_ENABLED;

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
        GameAssets3D.addTo(assets);
        pickerGameOver = Picker.fromBundle(assets.bundles().getLast(), "game.over");
        pickerLevelComplete = Picker.fromBundle(assets.bundles().getLast(), "level.complete");
        sound().setAssets(assets);
    }

    @Override
    protected GamePage3D createGamePage(Scene parentScene) {
        var gamePage = new GamePage3D(this, parentScene);
        gamePage.gameScenePy.bind(gameScenePy);
        return gamePage;
    }

    @Override
    protected StringBinding stageTitleBinding() {
        return Bindings.createStringBinding(() -> {
            String sceneName = currentGameScene().map(gameScene -> gameScene.getClass().getSimpleName()).orElse(null);
            String sceneNameText = sceneName != null && PY_DEBUG_INFO_VISIBLE.get() ? " [%s]".formatted(sceneName) : "";
            // resource key is composed of game variant, paused state and display mode (2D, 3D)
            String key = "app.title." + assetPrefix(gameVariant());
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