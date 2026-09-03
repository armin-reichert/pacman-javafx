package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

import java.util.Comparator;

public class RenderingComp implements GameEntityComp {

    public static final Comparator<RenderingComp> RENDERING_ORDER =
        Comparator.comparingInt((RenderingComp rc) -> rc.layer().z())
            .thenComparingInt(RenderingComp::layerPriority);

    private final RenderingLayer layer;
    private int layerPriority;

    public RenderingComp(RenderingLayer layer) {
        this.layer = layer;
    }

    public RenderingLayer layer() {
        return layer;
    }

    public int layerPriority() {
        return layerPriority;
    }

    public void setLayerPriority(int layerPriority) {
        this.layerPriority = layerPriority;
    }
}
