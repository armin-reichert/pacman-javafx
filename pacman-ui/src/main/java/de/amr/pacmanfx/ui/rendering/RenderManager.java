/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.miniview.MiniViewOverlayRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private final RenderQueue renderQueue = new RenderQueue();

    private Renderer variantRenderer;
    private Renderer levelRenderer;
    private Renderer sceneRenderer;
    private Renderer sceneDebugRenderer;
    private Renderer miniViewOverlayRenderer;

    private Vector2f currentOffset = Vector2f.ZERO;

    public RenderManager() {
        clearAllRenderers();
    }

    private void clearAllRenderers() {
        variantRenderer = Renderer.NULL_RENDERER;
        levelRenderer = Renderer.NULL_RENDERER;
        sceneRenderer = Renderer.NULL_RENDERER;
        sceneDebugRenderer = Renderer.NULL_RENDERER;
    }

    public void createRenderers(
        GameVariantPlayConfig playConfig,
        GameVariantRenderConfig renderConfig,
        AbstractGameScene gameScene,
        MiniPlaySceneView miniView)
    {
        requireNonNull(playConfig);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        clearAllRenderers();

        final ActorSpriteAnimController animController = playConfig.systems().actorSpriteAnimController();

        //TODO This is just a temporary solution
        miniViewOverlayRenderer = new MiniViewOverlayRenderer(miniView, animController, renderConfig);

        // If this scene has 2D rendering support, create and configure renderers
        final var rendering2D = gameScene.optCanvasRendering().orElse(null);
        if (rendering2D == null) {
            return;
        }

        final Canvas sceneCanvas = rendering2D.canvas();
        if (sceneCanvas == null) {
            return;
        }

        currentOffset = gameScene.renderOffset();

        variantRenderer    = renderConfig.createVariantRenderer(animController, sceneCanvas);
        sceneRenderer      = renderConfig.createGameSceneRenderer(gameScene, animController, sceneCanvas); // may return null!
        sceneDebugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, sceneCanvas);
        levelRenderer      = renderConfig.createGameLevelRenderer(animController, sceneCanvas);

        bindRendererProperties(sceneRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
        bindRendererProperties(variantRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
        bindRendererProperties(sceneDebugRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
        bindRendererProperties(levelRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
    }

    public RenderQueue renderQueue() {
        return renderQueue;
    }

    public void clearSceneCanvas(AbstractGameScene gameScene) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            final Canvas canvas = canvasRendering.canvas();
            if (canvas != null) {
                final var ctx = canvas.getGraphicsContext2D();
                ctx.setFill(canvasRendering.backgroundColor());
                ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        });
    }

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.sort();

        renderQueue.renderables().forEach(r -> {
            switch (r.layer()) {
                case MINIVIEW_OVERLAY -> miniViewOverlayRenderer.render(r, tick);
                case SCENE -> renderWithOffset(r, sceneRenderer, tick); //TODO get rid of scene renderers
                case LEVEL -> renderWithOffset(r, levelRenderer, tick);
                case HUD -> variantRenderer.render(r, tick);
                default -> renderWithOffset(r, variantRenderer, tick);
            }
        });

        if (debugMode) {
            renderQueue.renderables()
                .filter(r -> r.layer() == RenderingLayer.SCENE)
                .forEach(r -> sceneDebugRenderer.render(r, tick));
        }
    }

    private void renderWithOffset(Renderable r, Renderer renderer, long tick) {
        if (renderer != Renderer.NULL_RENDERER) {
            final GraphicsContext ctx = renderer.ctx();
            final Vector2f offset = currentOffset.scaled(renderer.scaling());
            ctx.save();
            ctx.translate(offset.x(), offset.y());
            renderer.render(r, tick);
            ctx.restore();
        }
    }

    private void bindRendererProperties(Renderer renderer, ObjectProperty<Color> backgroundColorProperty, DoubleProperty scalingProperty) {
        if (renderer != Renderer.NULL_RENDERER) {
            renderer.backgroundColorProperty().bind(backgroundColorProperty);
            renderer.scalingProperty().bind(scalingProperty);
        }
    }
}
