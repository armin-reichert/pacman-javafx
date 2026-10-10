/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.engine.config.GameVariantRenderConfig;
import de.amr.pacmanfx.engine.runtime.GameVariantRuntime;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class GamePlayViewRenderer {

    public static void render(GamePlayView playView, GameActionContext actionContext, AbstractGameScene gameScene) {
        requireNonNull(playView);
        requireNonNull(actionContext);
        requireNonNull(gameScene);

        final RenderManager renderManager = actionContext.renderManager();
        final GameVariantRuntime runtime = actionContext.gameVariantManager().currentRuntime();
        final GameViewModel viewModel = actionContext.ui().viewModel();
        final boolean debugMode = viewModel.debugModeOnProperty().get();

        renderManager.clearRenderQueue();

        // HUD
        final GameSession session = actionContext.currentGame().session();
        if (session.isHUDVisible()) {
            GameEntityViewBuilder.streamOfViews(session.hud().allEntities(), RenderingLayer.HUD)
                .forEach(renderManager::addRenderable);
        }

        // Mini view
        playView.layers().miniViewLayer().renderables().forEach(renderManager::addRenderable);

        // Game scene content
        gameScene.renderables().forEach(renderManager::addRenderable);

        // Debug mode rendering
        if (debugMode) {
            renderManager.addRenderable(new GameSceneDebugView(gameScene));
        }

        //TODO This should not be done in each render frame
        updateRenderers(
            renderManager,
            viewModel,
            runtime.playConfig().systems().actorSpriteAnimController(),
            runtime.uiConfig().renderConfig(),
            gameScene,
            gameScene.view2D(),
            playView.layers().miniViewLayer()
        );

        playView.layers().miniViewLayer().renderingSurface().fill(Color.BLACK);

        //TODO Rethink this (maybe add "clear canvas" command into queue?
        if (gameScene.view2D() != null && gameScene.view2D().autoClearCanvas()) {
            renderManager.variantRenderer().clearCanvas();
        }

        renderManager.renderFrame(actionContext.clock().currentTick(), debugMode);
    }

    private static void updateRenderers(
        RenderManager renderManager,
        GameViewModel viewModel,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig,
        GameScene gameScene,
        GameSceneView2D sceneRendering, // can be null!
        MiniPlaySceneView miniView)
    {
        if (sceneRendering != null) { // A game scene that can be rendered in 2D
            renderManager.setClipRect(sceneRendering.clipRect());
            final RenderingSurface renderingSurface = sceneRendering.renderingSurface();
            if (renderingSurface != null) {
                setRenderers(renderManager,
                    createVariantRenderer(
                        renderConfig,
                        animController,
                        renderingSurface,
                        viewModel.common2DSettings().canvasBackgroundColorProperty()
                    ),
                    createDebugRenderer(
                        gameScene,
                        renderConfig,
                        animController,
                        renderingSurface,
                        viewModel.common2DSettings().canvasBackgroundColorProperty()
                    )
                );
            }
        }
        else { // Assume game scene is 3D and mini view is active
            setRenderers(renderManager,
                createVariantRenderer(
                    renderConfig,
                    animController,
                    miniView.renderingSurface(),
                    viewModel.common2DSettings().canvasBackgroundColorProperty()
                ),
                // No debug rendering in mini view
                null
            );
        }
    }

    private static void setRenderers(RenderManager renderManager, Renderer variantRenderer, Renderer debugRenderer) {
        renderManager.setVariantRenderer(variantRenderer);
        renderManager.setDebugRenderer(debugRenderer);
    }

    private static Renderer createVariantRenderer(
        GameVariantRenderConfig renderConfig,
        ActorSpriteAnimController animController,
        RenderingSurface renderingSurface,
        ObjectProperty<Color> backgroundColorProperty)
    {
        final Renderer renderer = renderConfig.createVariantRenderer(animController, renderingSurface.canvas());
        renderer.backgroundColorProperty().bind(backgroundColorProperty);
        renderer.scalingProperty().bind(renderingSurface.scalingProperty());
        return renderer;
    }

    private static Renderer createDebugRenderer(
        GameScene gameScene,
        GameVariantRenderConfig renderConfig,
        ActorSpriteAnimController animController,
        RenderingSurface renderingSurface,
        ObjectProperty<Color> backgroundColorProperty)
    {
        final Renderer renderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, renderingSurface.canvas());
        renderer.backgroundColorProperty().bind(backgroundColorProperty);
        renderer.scalingProperty().bind(renderingSurface.scalingProperty());
        return renderer;
    }

}
