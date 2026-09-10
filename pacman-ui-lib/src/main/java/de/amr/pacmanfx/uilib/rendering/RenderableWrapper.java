package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public class RenderableWrapper implements Renderable {

    public static RenderableWrapper reassignLayer(Renderable r, RenderingLayer layer, int z) {
        return new RenderableWrapper(r, layer, z);
    };

    public static RenderableWrapper reassignLayer(Renderable r, RenderingLayer layer) {
        return new RenderableWrapper(r, layer, 0);
    };

    private final RenderingLayer targetLayer;
    private final Renderable renderable;
    private final int z;

    private RenderableWrapper(Renderable renderable, RenderingLayer targetLayer, int z) {
        this.renderable = renderable;
        this.targetLayer = targetLayer;
        this.z = z;
    }

    private RenderableWrapper(Renderable renderable, RenderingLayer targetLayer) {
        this(renderable, targetLayer, 0);
    }

    public Renderable wrappedRenderable() {
        return renderable;
    }

    @Override
    public RenderingLayer layer() {
        return targetLayer;
    }

    @Override
    public int z() {
        return z;
    }
}
