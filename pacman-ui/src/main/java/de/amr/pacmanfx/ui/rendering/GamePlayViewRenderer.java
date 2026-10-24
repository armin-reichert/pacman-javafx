/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.app.config.GameVariantRenderConfig;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class GamePlayViewRenderer {

    private final GamePlayView playView;

    public GamePlayViewRenderer(GamePlayView playView) {
        this.playView = requireNonNull(playView);
    }

    public void render(EngineUIContext engine) {
        requireNonNull(engine);
        if (engine.optCurrentGame().isEmpty()) {
            return;
        }
        if (!(engine.gameSceneManager().currentGameScene() instanceof AbstractGameScene gameScene)) {
            return;
        }
        render(
            engine,
            engine.renderManager(),
            engine.ui().viewModel(),
            engine.gameVariantManager().currentRuntime(),
            engine.optCurrentGame().get().session(),
            gameScene,
            engine.clock().currentTick()
        );
    }

    private void render(
        EngineUIContext engine,
        RenderManager renderManager,
        GameViewModel viewModel,
        GameVariantRuntime runtime,
        GameSession session,
        AbstractGameScene gameScene,
        long tick)
    {
        // --- Refill the render queue

        renderManager.clearRenderQueue();

        // HUD
        if (session.booleanValue(SessionKey.HUD_VISIBLE)) {
            GameEntityViewBuilder.streamOfViews(session.hud().allEntities(), RenderingLayer.HUD)
                .forEach(renderManager::addRenderable);
        }

        // Mini view
        playView.layers().miniViewLayer().renderables().forEach(renderManager::addRenderable);

        // Game scene content
        gameScene.renderables().forEach(renderManager::addRenderable);

        // Debug mode rendering?
        final boolean debugMode = viewModel.debugModeOnProperty().get();
        if (debugMode) {
            renderManager.addRenderable(new GameSceneDebugView(gameScene));
        }

        // --- Update the renderers
        //TODO This should not be done in every render frame!

        final GameVariantRenderConfig renderConfig = engine.uiConfig(runtime).renderConfig();
        final ActorSpriteAnimController animController = runtime.systems().actorSpriteAnimController();

        if (gameScene.view2D() != null) {
            final RenderingSurface renderingSurface = gameScene.view2D().renderingSurface();
            if (renderingSurface != null) {
                renderManager.setVariantRenderer(createVariantRenderer(
                    renderConfig,
                    animController,
                    renderingSurface,
                    viewModel.common2DSettings().canvasBackgroundColorProperty()
                ));
                if (gameScene.view2D().autoClearCanvas()) {
                    renderManager.variantRenderer().clearCanvas();
                }
                renderManager.setDebugRenderer(createDebugRenderer(
                    gameScene,
                    renderConfig,
                    animController,
                    renderingSurface,
                    viewModel.common2DSettings().canvasBackgroundColorProperty()
                ));
                renderManager.setClipRect(gameScene.view2D().clipRect());
            }
        }
        else {
            renderManager.setVariantRenderer(createVariantRenderer(
                renderConfig,
                animController,
                playView.layers().miniViewLayer().renderingSurface(),
                viewModel.common2DSettings().canvasBackgroundColorProperty()
            ));
            renderManager.setDebugRenderer(null); // No debug rendering in mini view
            playView.layers().miniViewLayer().renderingSurface().fill(Color.BLACK);
        }

        renderManager.renderFrame(tick, debugMode);
    }

    private Renderer createVariantRenderer(
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

    private Renderer createDebugRenderer(
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
