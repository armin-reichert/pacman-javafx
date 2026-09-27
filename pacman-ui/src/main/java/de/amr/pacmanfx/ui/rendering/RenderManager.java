/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.miniview.MiniViewOverlayRenderer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
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

    public void updateRenderers(
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig,
        GameScene gameScene,
        GameSceneCanvasRenderingComp canvasRendering,
        MiniPlaySceneView miniView)
    {
        requireNonNull(animController);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        clearAllRenderers();

        //TODO This is just a temporary solution
        miniViewOverlayRenderer = new MiniViewOverlayRenderer(miniView, animController, renderConfig);

        // If this scene has 2D rendering support, create and configure renderers
        if (canvasRendering != null) {
            final Canvas canvas = canvasRendering.canvas();
            if (canvas != null) {
                variantRenderer = renderConfig.createVariantRenderer(animController, canvas);
                sceneDebugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, canvas);
                levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
            }
            bindRendererProperties(variantRenderer,    canvasRendering.backgroundColorProperty(), canvasRendering.scalingProperty());
            bindRendererProperties(sceneDebugRenderer, canvasRendering.backgroundColorProperty(), canvasRendering.scalingProperty());
            bindRendererProperties(levelRenderer,      canvasRendering.backgroundColorProperty(), canvasRendering.scalingProperty());
        }
    }

    public Renderer variantRenderer() {
        return variantRenderer;
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

    // Takes optional offset of renderable into account (in Tengen for example, the game level has horizontal offset)
    private void doRender(Renderable r, Renderer renderer, long tick) {
        if (renderer == null) {
            return;
        }

        final boolean needsTranslate = !r.offset().equals(Vector2f.ZERO);
        if (needsTranslate) {
            final Vector2f translate = r.offset().scaled(renderer.scaling());
            renderer.ctx().save();
            renderer.ctx().translate(translate.x(), translate.y());
        }

        renderer.render(r, tick);

        if (needsTranslate) {
            renderer.ctx().restore();
        }
    }

    private void bindRendererProperties(Renderer renderer, ObjectProperty<Color> backgroundColorProperty, DoubleProperty scalingProperty) {
        if (renderer != null) {
            renderer.backgroundColorProperty().bind(backgroundColorProperty);
            renderer.scalingProperty().bind(scalingProperty);
        }
    }
}
