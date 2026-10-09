/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.engine.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private final RenderQueue renderQueue = new RenderQueue();

    private Renderer variantRenderer;
    private Renderer debugRenderer;

    private RectShort clipRect;

    public RenderManager() {}

    public void clearRenderQueue() {
        renderQueue.clear();
    }

    public int renderQueueSize() {
        return renderQueue.size();
    }

    public void addRenderable(Renderable renderable) {
        renderQueue.add(renderable);
    }

    public void updateRenderers(
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
            clipRect = sceneRendering.clipRect();
            final RenderingSurface sceneRenderingSurface = sceneRendering.renderingSurface();
            if (sceneRenderingSurface != null) {
                variantRenderer = renderConfig.createVariantRenderer(animController, sceneRenderingSurface.canvas());

                variantRenderer.backgroundColorProperty().bind(sceneRenderingSurface.backgroundColorProperty());
                variantRenderer.scalingProperty().bind(sceneRenderingSurface.scalingProperty());

                debugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, sceneRenderingSurface.canvas());
                debugRenderer.backgroundColorProperty().bind(sceneRenderingSurface.backgroundColorProperty());
                debugRenderer.scalingProperty().bind(sceneRenderingSurface.scalingProperty());
            }
        }
        else {
            // Assume game scene is 3D scene and mini view is active
            variantRenderer = renderConfig.createVariantRenderer(animController, miniView.renderingSurface().canvas());
            variantRenderer.backgroundColorProperty().bind(miniView.renderingSurface().backgroundColorProperty());
            variantRenderer.scalingProperty().bind(miniView.renderingSurface().scalingProperty());

            // No debug rendering in mini view
            debugRenderer = null;
        }
    }

    public Renderer variantRenderer() {
        return variantRenderer;
    }

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.sort();
        renderQueue.renderables().forEach(r -> render(r, tick));
        if (debugMode) {
            renderQueue.renderables()
                .filter(r -> r.layer() == RenderingLayer.DEBUG)
                .forEach(r -> debugRenderer.render(r, tick));
        }
    }

    private void render(Renderable r, long tick) {
        final Renderer renderer = r.layer() == RenderingLayer.DEBUG ? debugRenderer : variantRenderer;

        final boolean hasOffset = !r.offset().equals(Vector2f.ZERO);
        if (hasOffset) {
            final Vector2f translate = r.offset().scaled(renderer.scaling());
            renderer.ctx().save();
            renderer.ctx().translate(translate.x(), translate.y());
        }

        // This supports scenes defining a clip rectangle as for example the Tengen 2D play scene does
        if (clipRect != null && r.layer() != RenderingLayer.DEBUG && r.layer() != RenderingLayer.MINI_VIEW_OVERLAY) {
            final double s = renderer.scaling();
            final Node clipNode = new Rectangle(s * clipRect.x(), s * clipRect.y(), s * clipRect.width(), s * clipRect.height());
            renderer.ctx().save();
            renderer.canvas().setClip(clipNode);
            renderer.render(r, tick);
            renderer.ctx().restore();
        }
        else {
            renderer.render(r, tick);
        }

        if (hasOffset) {
            renderer.ctx().restore();
        }
    }
}
