package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;

public class RenderableWrapper implements Renderable {

    private final RenderingLayer targetLayer;
    private final Renderable renderable;

    public RenderableWrapper(Renderable renderable, RenderingLayer targetLayer) {
        this.renderable = renderable;
        this.targetLayer = targetLayer;
    }

    public Renderable wrappedRenderable() {
        return renderable;
    }

    @Override
    public RenderingLayer layer() {
        return targetLayer;
    }
}
