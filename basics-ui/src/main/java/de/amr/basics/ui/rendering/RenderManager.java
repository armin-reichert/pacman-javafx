/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public class RenderManager {

    private final RenderQueue renderQueue = new RenderQueue();

    private Renderer variantRenderer;
    private Renderer debugRenderer;

    private RectShort clipRect;

    public RenderManager() {}

    public void setVariantRenderer(Renderer variantRenderer) {
        this.variantRenderer = variantRenderer;
    }

    public Renderer debugRenderer() {
        return debugRenderer;
    }

    public void setDebugRenderer(Renderer debugRenderer) {
        this.debugRenderer = debugRenderer;
    }

    public RectShort clipRect() {
        return clipRect;
    }

    public void setClipRect(RectShort clipRect) {
        this.clipRect = clipRect;
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
