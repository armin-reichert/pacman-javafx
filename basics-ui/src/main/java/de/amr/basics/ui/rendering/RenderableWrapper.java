/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

public class RenderableWrapper implements Renderable {

    public static RenderableWrapper assignLayer(Renderable r, RenderingLayer layer, int z) {
        return new RenderableWrapper(r, layer, z);
    }

    public static RenderableWrapper assignLayer(Renderable r, RenderingLayer layer) {
        return new RenderableWrapper(r, layer, 0);
    }

    private final RenderingLayer targetLayer;
    private final Renderable renderable;
    private final int z;

    private RenderableWrapper(Renderable renderable, RenderingLayer targetLayer, int z) {
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
