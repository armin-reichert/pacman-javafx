package de.amr.basics.ui.rendering;

public class RenderableObject implements Renderable {

    public static RenderableObject reordered(Renderable r, RenderingLayer layer, int z) {
        return new RenderableObject(r, layer, z);
    }

    public static RenderableObject reordered(Renderable r, RenderingLayer layer) {
        return new RenderableObject(r, layer, 0);
    }

    private final RenderingLayer targetLayer;
    private final Renderable renderable;
    private final int z;

    private RenderableObject(Renderable renderable, RenderingLayer targetLayer, int z) {
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
