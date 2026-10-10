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
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class GamePlayViewRenderer {

    public static void render(GamePlayView playView, GameEngineContext engineContext, GameScene gameScene) {
        requireNonNull(playView);
        requireNonNull(engineContext);
        requireNonNull(gameScene);

        final RenderManager renderManager = engineContext.renderManager();

        // --- Refill the render queue

        renderManager.clearRenderQueue();

        // HUD
        final GameSession session = engineContext.currentGame().session();
        if (session.isHUDVisible()) {
            GameEntityViewBuilder.streamOfViews(session.hud().allEntities(), RenderingLayer.HUD)
                .forEach(renderManager::addRenderable);
        }

        // Mini view
        playView.layers().miniViewLayer().renderables().forEach(renderManager::addRenderable);

        // Game scene content
        gameScene.renderables().forEach(renderManager::addRenderable);

        // Debug mode rendering?
        final GameViewModel viewModel = engineContext.ui().viewModel();
        final boolean debugMode = viewModel.debugModeOnProperty().get();
        if (debugMode) {
            renderManager.addRenderable(new GameSceneDebugView(gameScene));
        }

        // --- Update the renderers

        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            return; // Should never happen
        }

        //TODO This should not be done in each render frame

        final GameVariantRuntime runtime = engineContext.gameVariantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();
        final GameSceneView2D view2D = abstractGameScene.view2D();

        if (view2D != null) {
            final RenderingSurface renderingSurface = view2D.renderingSurface();
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
                if (view2D.autoClearCanvas()) {
                    renderManager.variantRenderer().clearCanvas();
                }
                renderManager.setClipRect(view2D.clipRect());
            }
        }
        else {
            setRenderers(renderManager,
                createVariantRenderer(
                    renderConfig,
                    animController,
                    playView.layers().miniViewLayer().renderingSurface(),
                    viewModel.common2DSettings().canvasBackgroundColorProperty()
                ),
                // No debug rendering in mini view
                null
            );
            playView.layers().miniViewLayer().renderingSurface().fill(Color.BLACK);
        }

        // --- Render everything

        renderManager.renderFrame(engineContext.clock().currentTick(), debugMode);
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
