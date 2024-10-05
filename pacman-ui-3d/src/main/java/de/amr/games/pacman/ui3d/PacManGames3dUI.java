/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
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
        sounds().setAssets(assets);
    }

    protected void registerActions() {
        for (GameAction action : GameAction2D.values()) {
            KEYBOARD.register(action.trigger());
        }
        for (GameAction action : GameAction3D.values()) {
            KEYBOARD.register(action.trigger());
        }
    }


    @Override
    public void setGameScenes(GameVariant variant, Map<GameSceneID, GameScene> gameScenes) {
        super.setGameScenes(variant, gameScenes);
        // init 3D play scene if present
        GameScene gameScene = gameScenes.get(GameSceneID.PLAY_SCENE_3D);
        if (gameScene == null) {
            Logger.error("No play scene 3D has been registered.");
            return;
        }
        if (gameScene instanceof PlayScene3D playScene3D) {
            playScene3D.setContext(this);
            playScene3D.widthProperty().bind(rootPane().widthProperty());
            playScene3D.heightProperty().bind(rootPane().heightProperty());
        } else {
            Logger.error("Something is wrong: Game scene registered as 3D play scene has unsupported class {}", gameScene.getClass());
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
}