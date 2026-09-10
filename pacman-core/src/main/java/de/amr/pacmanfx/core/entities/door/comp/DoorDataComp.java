/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.door.comp;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class DoorDataComp implements GameEntityComp {

    private Vector2i leftTile;

    private Vector2i rightTile;

    private String color; // no really "layout"...

    public Vector2i leftTile() {
        return leftTile;
    }

    public void setLeftTile(Vector2i leftTile) {
        this.leftTile = leftTile;
    }

    public Vector2i rightTile() {
        return rightTile;
    }

    public void setRightTile(Vector2i rightTile) {
        this.rightTile = rightTile;
    }

    public String color() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
