package de.amr.pacmanfx.core;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public class Energizer extends GameEntity implements Renderable {

    private final Vector2i tile;

    private boolean on;

    public Energizer(Vector2i tile) {
        this.tile = tile;
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.WORLD;
    }

    public boolean on() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public Vector2i tile() {
        return tile;
    }
}
