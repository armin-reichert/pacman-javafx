package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;

public class RenderingReorderWrapper implements Renderable {

    public static RenderingReorderWrapper reorder(Renderable r, RenderingLayer layer, int z) {
        return new RenderingReorderWrapper(r, layer, z);
    }

    public static RenderingReorderWrapper reorder(Renderable r, RenderingLayer layer) {
        return new RenderingReorderWrapper(r, layer, 0);
    }

    private final RenderingLayer targetLayer;
    private final Renderable renderable;
    private final int z;

    private RenderingReorderWrapper(Renderable renderable, RenderingLayer targetLayer, int z) {
        this.renderable = renderable;
        this.targetLayer = targetLayer;
        this.z = z;
    }

    public Renderable content() {
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
