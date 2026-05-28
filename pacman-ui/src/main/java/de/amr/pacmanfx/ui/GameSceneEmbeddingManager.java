/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.layout.playview.DecorationPane;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class GameSceneEmbeddingManager {

    private final GameUI ui;

    public GameSceneEmbeddingManager(GameUI ui) {
        this.ui = Objects.requireNonNull(ui);
    }

    public void removeFromPlayView(GameScene gameScene) {
        requireNonNull(gameScene);

        ui.viewManager().playView().contextMenu().hide();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });
        if (gameScene instanceof GameScene2D gameScene2D) {
            final DecorationPane decorationPane = ui.viewManager().playView().decorationPane();
            decorationPane.canvas().widthProperty().unbind();
            decorationPane.canvas().heightProperty().unbind();
            decorationPane.unscaledWidthProperty().unbind();
            decorationPane.unscaledHeightProperty().unbind();
            decorationPane.backgroundProperty().unbind();
            gameScene2D.backgroundColorProperty().unbind();
            gameScene2D.scalingProperty().unbind();
        }

        Logger.info("Game scene {} REMOVED from play scene!", gameScene.getClass().getSimpleName());
    }

    public void embedGameSceneIntoPlayView(GameScene gameScene) {
        ui.viewManager().playView().contextMenu().hide();

        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(ui.scene(), ui.viewManager().playView(), gameScene, gameScene.optSubSceneFX().get());
        } else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(ui.scene(), ui.viewManager().playView(), ui.currentConfig().gameSceneConfig(), gameScene2D);
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

        gameScene2D.backgroundColorProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene2D);
        if (decorated) {
            decorationPane.newCanvas(); //TODO check why creating a new canvas is needed

            decorationPane.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(UfxBackgrounds::paintBackground));

            // set unscaled decoration pane size to game scene (=world map) size
            decorationPane.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            decorationPane.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // Limit scaling
            gameScene2D.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), PlayView.MAX_GAME_SCENE_SCALING)));

            decorationPane.stretchTo(scene.getWidth(), scene.getHeight());

            playView.setGameSceneContent(decorationPane);
        }
        else {
            // Undecorated game scene taking complete height
            decorationPane.canvas().heightProperty().bind(scene.heightProperty());
            decorationPane.canvas().widthProperty().bind(scene.heightProperty().map(h -> h.doubleValue() * gameScene2D.getAspectRatio()));
            gameScene2D.scalingProperty().bind(scene.heightProperty().divide(gameScene2D.getUnscaledHeight()));

            playView.setGameSceneContent(decorationPane.canvas());
        }

        gameScene2D.setCanvas(decorationPane.canvas());
        playView.updateGameSceneRenderers(gameScene2D);
    }
}
