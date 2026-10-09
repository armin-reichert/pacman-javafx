/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.engine.config.GameVariantRenderConfig;
import de.amr.pacmanfx.engine.runtime.GameVariantRuntime;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;

import static java.util.Objects.requireNonNull;

public class GamePlayViewRenderer {

    public static void render(GamePlayView playView, PacManGamesEngine app, GameClock clock, AbstractGameScene gameScene) {
        final GameVariantRuntime runtime = app.gameVariantManager().currentRuntime();
        final RenderManager renderManager = app.renderManager();
        final boolean debugMode = app.ui().viewModel().debugModeOnProperty().get();

        fillRenderQueue(playView, renderManager, app.currentGame(), gameScene, debugMode);

        //TODO This should not be done in each render frame
        updateRenderers(
            renderManager,
            runtime.playConfig().systems().actorSpriteAnimController(),
            runtime.uiConfig().renderConfig(),
            gameScene,
            gameScene.view2D(),
            playView.layers().miniViewLayer()
        );

        // Clear canvases
        playView.layers().miniViewLayer().renderingSurface().clear();

        //TODO Rethink this (maybe add "clear canvas" command into queue?
        if (gameScene.view2D() != null && gameScene.view2D().autoClearCanvas()) {
            renderManager.variantRenderer().clearCanvas();
        }

        renderManager.renderFrame(clock.currentTick(), debugMode);
    }

    public static void updateRenderers(
        RenderManager renderManager,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig,
        GameScene gameScene,
        GameSceneView2D sceneRendering, // can be null!
        MiniPlaySceneView miniView)
    {
        requireNonNull(animController);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        if (sceneRendering != null) {
            // A game scene that can be rendered in 2D
            renderManager.setClipRect(sceneRendering.clipRect());
            final RenderingSurface sceneRenderingSurface = sceneRendering.renderingSurface();
            if (sceneRenderingSurface != null) {
                final Renderer variantRenderer = renderConfig.createVariantRenderer(animController, sceneRenderingSurface.canvas());
                variantRenderer.backgroundColorProperty().bind(sceneRenderingSurface.backgroundColorProperty());
                variantRenderer.scalingProperty().bind(sceneRenderingSurface.scalingProperty());
                renderManager.setVariantRenderer(variantRenderer);

                final Renderer debugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, sceneRenderingSurface.canvas());
                debugRenderer.backgroundColorProperty().bind(sceneRenderingSurface.backgroundColorProperty());
                debugRenderer.scalingProperty().bind(sceneRenderingSurface.scalingProperty());
                renderManager.setDebugRenderer(debugRenderer);
            }
        }
        else {
            // Assume game scene is 3D scene and mini view is active
            final Renderer variantRenderer = renderConfig.createVariantRenderer(animController, miniView.renderingSurface().canvas());
            variantRenderer.backgroundColorProperty().bind(miniView.renderingSurface().backgroundColorProperty());
            variantRenderer.scalingProperty().bind(miniView.renderingSurface().scalingProperty());
            renderManager.setVariantRenderer(variantRenderer);

            // No debug rendering in mini view
            renderManager.setDebugRenderer(null);
        }
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
