/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.util.Picker;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;

import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
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
public class PacManGames3dUI extends PacManGames2dUI {

    public void loadAssets() {
        GameAssets2D.addTo(assets);
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
    protected ObservableValue<String> stageTitleBinding() {
        return Bindings.createStringBinding(() -> {
            String sceneName = currentGameScene().map(gameScene -> gameScene.getClass().getSimpleName()).orElse(null);
            String sceneNameSuffix = sceneName != null && PY_DEBUG_INFO_VISIBLE.get() ? " [%s]".formatted(sceneName) : "";
            // resource key is composed of game variant, paused state and display mode (2D, 3D)
            String gameVariantPart = "app.title." + assetPrefix(gameVariantPy.get());
            String pausedPart = clock.pausedPy.get() ? ".paused" : "";
            String displayMode = locText(PY_3D_ENABLED.get() ? "threeD" : "twoD");
            return locText(gameVariantPart + pausedPart, displayMode) + sceneNameSuffix;
        }, clock.pausedPy, gameVariantPy, PY_3D_ENABLED, gameScenePy, PY_DEBUG_INFO_VISIBLE);
    }

    @Override
    public void togglePlayScene2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            if (currentGameSceneHasID("PlayScene2D") || currentGameSceneHasID("PlayScene3D")) {
                updateGameScene(true);
                gameScene.onSceneVariantSwitch(gameScene);
            }
            gameController().update();
            if (!game().isPlaying()) {
                showFlashMessage(locText(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }
}