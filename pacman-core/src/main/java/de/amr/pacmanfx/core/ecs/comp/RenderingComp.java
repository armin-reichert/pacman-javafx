package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class RenderingComp implements GameEntityComp {

    private final RenderingLayer layer;

    public RenderingComp(RenderingLayer layer) {
        this.layer = layer;
    }

    public RenderingLayer zLayer() {
        return layer;
    }
}
