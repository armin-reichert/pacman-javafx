/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneRendering2DComp;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;

public class GamePlayViewRenderer {

    public static void render(GamePlayView playView, GameApp app, GameClock clock, AbstractGameScene gameScene) {
        final GameVariantRuntime runtime = app.variantManager().currentRuntime();
        final RenderManager renderManager = app.renderManager();
        final boolean debugMode = app.ui().viewModel().debugModeOnProperty().get();

        fillRenderQueue(playView, renderManager, app.game(), gameScene, debugMode);

        final GameSceneRendering2DComp r2d = gameScene.optRendering2D().orElse(null);

        //TODO This should not be done in each render frame
        renderManager.updateRenderers(
            runtime.playConfig().systems().actorSpriteAnimController(),
            runtime.uiConfig().renderConfig(),
            gameScene,
            r2d,
            playView.layers().miniViewLayer()
        );

        // Clear canvases
        playView.layers().miniViewLayer().renderingSurface().clear();

        //TODO Rethink this (maybe add "clear canvas" command into queue?
        if (r2d != null && r2d.autoClearCanvas()) {
            renderManager.variantRenderer().clearCanvas();
        }

        renderManager.renderFrame(clock.currentTick(), debugMode);
    }

    private static void fillRenderQueue(GamePlayView playView, RenderManager renderManager, GameContext game, GameScene gameScene, boolean debugMode) {
        renderManager.clearRenderQueue();

        // HUD
        final GameSession session = game.session();
        if (session.isHUDVisible()) {
            GameEntityViewBuilder.streamOfViews(session.hud().allEntities(), RenderingLayer.HUD)
                .forEach(renderManager::addRenderable);
        }

        // Mini view
        playView.layers().miniViewLayer().renderables().forEach(renderManager::addRenderable);

        // Game scene content
        if (gameScene != null) {
            gameScene.renderables().forEach(renderManager::addRenderable);
        }

        // Debug mode rendering
        if (debugMode) {
            renderManager.addRenderable(new GameSceneDebugView(gameScene));
        }
    }
}
