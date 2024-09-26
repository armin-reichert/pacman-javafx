/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.shape.DrawMode;

import java.util.Map;

import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

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

    public PacManGames3dUI() {
        GameAssets2D.load(() -> PacManGames2dUI.class, assets);
        GameAssets3D.load(() -> PacManGames3dUI.class, assets);
        GameSounds.setAssets(assets);
    }

    @Override
    public void setGameScenes(Map<GameVariant, Map<GameSceneID, GameScene>> gameSceneMap) {
        super.setGameScenes(gameSceneMap);
        for (GameVariant variant : GameVariant.values()) {
            var playScene3D = (PlayScene3D) gameSceneMap.get(variant).get(GameSceneID.PLAY_SCENE_3D);
            playScene3D.setContext(this);
            playScene3D.widthProperty().bind(rootPane().widthProperty());
            playScene3D.heightProperty().bind(rootPane().heightProperty());
        }
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
            // resource key is composed of game variant, paused state and display mode (2D, 3D)
            String gameVariantPart = "app.title." + assetPrefix(gameVariantPy.get());
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
    public void selectNext3DPerspective() {
        var next = Perspective.next(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(next);
        showFlashMessage(locText("camera_perspective", locText(next.name())));
    }

    @Override
    public void selectPrev3DPerspective() {
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