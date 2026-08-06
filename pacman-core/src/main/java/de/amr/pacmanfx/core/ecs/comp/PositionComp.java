/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public final class PositionComp implements GameEntityComponent {

    private float x;
    private float y;

    @Override
    public void reset() {
        x = 0f;
        y = 0f;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public void setX(double x) {
        this.x = (float) x;
    }

    public void setY(double y) {
        this.y = (float) y;
    }

    public void set(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public void set(Vector2f v) {
        x = v.x();
        y = v.y();
    }

    public void add(float dx, float dy) {
        x += dx;
        y += dy;
    }

    public void subtract(float dx, float dy) {
        x -= dx;
        y -= dy;
    }

    public Vector2f center(int tileSize) {
        final float halfTileSize = 0.5f * tileSize;
        return new Vector2f(x + halfTileSize, y + halfTileSize);
    }

    public Vector2f asVector2f() {
        return new Vector2f(x, y);
    }

    @Override
    public String toString() {
        return "Position{" +
            "x=" + x +
            ", y=" + y +
            '}';
    }
}
