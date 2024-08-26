/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.shape.DrawMode;

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
    protected GamePage3D createGamePage(Scene parentScene) {
        var gamePage = new GamePage3D(this, parentScene);
        gamePage.gameScenePy.bind(gameScenePy);
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