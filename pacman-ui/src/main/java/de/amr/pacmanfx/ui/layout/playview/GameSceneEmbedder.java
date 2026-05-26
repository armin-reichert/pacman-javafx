/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

public class GameSceneEmbedder {

    public void embedGameSceneIntoPlayView(Scene scene, PlayView playView, GameSceneConfig gameSceneConfig, GameScene gameScene) {
        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(scene, playView, gameScene, gameScene.optSubSceneFX().get());
        } else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(scene, playView, gameSceneConfig, gameScene2D);
        } else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(Scene scene, PlayView playView, GameScene gameScene, SubScene subSceneFX) {
        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(scene.widthProperty());
        subSceneFX.heightProperty().bind(scene.heightProperty());

        if (gameScene instanceof GameScene2D gameScene2D) {
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            gameScene2D.setCanvas(playView.decorationPane().canvas());
            playView.updateGameSceneRenderers(gameScene2D);
        }
        playView.setGameSceneContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(Scene scene, PlayView playView, GameSceneConfig gameSceneConfig, GameScene2D gameScene2D) {
        final DecorationPane decorationPane = playView.decorationPane();
        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene2D);

        if (decorated) {
            // set unscaled decoration pane size to game scene (=world map) size
            decorationPane.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            decorationPane.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // scale decoration pane to available scene space
            decorationPane.stretchTo(scene.getWidth(), scene.getHeight());

            // bind background color for canvas and decoration pane
            gameScene2D.backgroundColorProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);
            decorationPane.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(UfxBackgrounds::paintBackground));

            // Limit scaling
            gameScene2D.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), PlayView.MAX_GAME_SCENE_SCALING)));

            decorationPane.newCanvas(); //TODO check why creating a new canvas is needed
            gameScene2D.setCanvas(decorationPane.canvas());
            playView.updateGameSceneRenderers(gameScene2D);

            playView.setGameSceneContent(decorationPane);

        }
        else { // Undecorated game scene taking complete height

            final Canvas canvas = decorationPane.canvas();
            final double aspect = gameScene2D.getAspectRatio();

            canvas.heightProperty().bind(scene.heightProperty());
            canvas.widthProperty().bind(scene.heightProperty().map(h -> h.doubleValue() * aspect));

            gameScene2D.scalingProperty().bind(scene.heightProperty().divide(gameScene2D.getUnscaledHeight()));

            // Game scene renderer can only be created if canvas is available
            gameScene2D.setCanvas(canvas);
            playView.updateGameSceneRenderers(gameScene2D);

            playView.setGameSceneContent(canvas);
        }
    }
}
