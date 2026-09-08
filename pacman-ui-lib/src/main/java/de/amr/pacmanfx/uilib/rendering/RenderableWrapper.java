package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public class RenderableWrapper implements Renderable {

    private final RenderingLayer targetLayer;
    private final Renderable renderable;
    private final int z;

    public RenderableWrapper(Renderable renderable, RenderingLayer targetLayer, int z) {
        this.renderable = renderable;
        this.targetLayer = targetLayer;
        this.z = z;
    }

    public RenderableWrapper(Renderable renderable, RenderingLayer targetLayer) {
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
