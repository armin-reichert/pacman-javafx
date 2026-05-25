/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

public class PlayViewGameSceneEmbedder {

    public void embedGameScene(GameUI ui, PlayView playView, GameScene gameScene) {
        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(playView, gameScene, gameScene.optSubSceneFX().get());
        } else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(ui, playView, gameScene2D);
        } else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }


    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(PlayView playView, GameScene gameScene, SubScene subSceneFX) {
        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(playView.parentSceneFX().widthProperty());
        subSceneFX.heightProperty().bind(playView.parentSceneFX().heightProperty());

        if (gameScene instanceof GameScene2D gameScene2D) {
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            gameScene2D.setCanvas(playView.decorationPane().canvas());
            playView.updateRenderers(gameScene2D);
        }
        playView.setContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(GameUI ui, PlayView playView, GameScene2D gameScene2D) {
        final boolean decorated = ui.currentGameSceneConfig().sceneDecorationRequested(gameScene2D);
        final GameSceneDecorationPane decorationPane = playView.decorationPane();

        if (decorated) {
            // set unscaled decoration pane size to game scene (=world map) size
            decorationPane.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            decorationPane.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // scale decoration pane to available scene space
            decorationPane.stretchTo(playView.parentSceneFX().getWidth(), playView.parentSceneFX().getHeight());

            // bind background color for canvas and decoration pane
            gameScene2D.backgroundColorProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);
            decorationPane.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(UfxBackgrounds::paintBackground));

            // Limit scaling
            gameScene2D.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), PlayView.MAX_GAME_SCENE_SCALING)));

            decorationPane.newCanvas(); //TODO check why creating a new canvas is needed
            gameScene2D.setCanvas(decorationPane.canvas());
            playView.updateRenderers(gameScene2D);

            playView.canvasLayer().setCenter(decorationPane);
        } else {
            // Undecorated game scene taking complete height

            final Canvas canvas = decorationPane.canvas();
            final double aspect = gameScene2D.getAspectRatio();

            canvas.heightProperty().bind(playView.parentSceneFX().heightProperty());
            canvas.widthProperty().bind(playView.parentSceneFX().heightProperty().map(h -> h.doubleValue() * aspect));

            gameScene2D.scalingProperty().bind(playView.parentSceneFX().heightProperty().divide(gameScene2D.getUnscaledHeight()));

            // Game scene renderer can only be created if canvas is available
            gameScene2D.setCanvas(canvas);
            playView.updateRenderers(gameScene2D);

            playView.canvasLayer().setCenter(canvas);
        }

        playView.setContent(playView.canvasLayer());
    }
}
