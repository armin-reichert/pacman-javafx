package de.amr.pacmanfx.core;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.Vector2i;

public class Energizer extends GameEntity {

    private final Vector2i tile;

    private boolean on;

    public Energizer(Vector2i tile) {
        this.tile = tile;
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
