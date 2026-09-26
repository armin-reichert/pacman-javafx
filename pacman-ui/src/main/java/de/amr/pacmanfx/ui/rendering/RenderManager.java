/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
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
    private Renderer sceneDebugRenderer;
    private Renderer miniViewOverlayRenderer;

    public RenderManager() {
        clearAllRenderers();
    }

    public void clearRenderQueue() {
        renderQueue.clear();
    }

    public int renderQueueSize() {
        return renderQueue.size();
    }

    public void addRenderable(Renderable renderable) {
        renderQueue.add(renderable);
    }

    private void clearAllRenderers() {
        variantRenderer = null;
        levelRenderer = null;
        sceneDebugRenderer = null;
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

        variantRenderer    = renderConfig.createVariantRenderer(animController, sceneCanvas);
        sceneDebugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, sceneCanvas);
        levelRenderer      = renderConfig.createGameLevelRenderer(animController, sceneCanvas);

        bindRendererProperties(variantRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
        bindRendererProperties(sceneDebugRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
        bindRendererProperties(levelRenderer, rendering2D.backgroundColorProperty(), rendering2D.scalingProperty());
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
            final Renderer renderer = selectRenderer(r);
            doRender(r, renderer, tick);
        });
        if (debugMode) {
            renderQueue.renderables()
                .filter(r -> r.layer() == RenderingLayer.DEBUG)
                .forEach(r -> sceneDebugRenderer.render(r, tick));
        }
    }

    //TODO this renderer per layer design is not the last word
    private Renderer selectRenderer(Renderable r) {
        return switch (r.layer()) {
            case DEBUG -> sceneDebugRenderer;
            case MINI_VIEW_OVERLAY -> miniViewOverlayRenderer;
            case LEVEL -> levelRenderer;
            default -> variantRenderer;
        };
    }

    // Takes offset of renderable into account (in Tengen for example, the game level has horizontal offset)
    private void doRender(Renderable r, Renderer renderer, long tick) {
        if (renderer != null) {
            final Vector2f offset = r.offset().scaled(renderer.scaling());
            final GraphicsContext ctx = renderer.ctx();
            ctx.save();
            ctx.translate(offset.x(), offset.y());
            renderer.render(r, tick);
            ctx.restore();
        }
    }

    private void bindRendererProperties(Renderer renderer, ObjectProperty<Color> backgroundColorProperty, DoubleProperty scalingProperty) {
        if (renderer != null) {
            renderer.backgroundColorProperty().bind(backgroundColorProperty);
            renderer.scalingProperty().bind(scalingProperty);
        }
    }
}
