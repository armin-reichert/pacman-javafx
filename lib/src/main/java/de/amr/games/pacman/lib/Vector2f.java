/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import static de.amr.games.pacman.lib.Globals.differsAtMost;

/**
 * Immutable 2D vector with float precision. Component values are treated as equal if they differ less than
 * {@link #EPSILON}.
 *
 * @author Armin Reichert
 */
public record Vector2f(float x, float y) {

    public static final Vector2f ZERO = new Vector2f(0, 0);

    public static final float EPSILON = 1e-6f;

    public Vector2f plus(Vector2f v) {
        return new Vector2f(x + v.x, y + v.y);
    }

    public Vector2f plus(float vx, float vy) {
        return new Vector2f(x + vx, y + vy);
    }

    public Vector2f minus(Vector2f v) {
        return new Vector2f(x - v.x, y - v.y);
    }

    public Vector2f minus(float vx, float vy) {
        return new Vector2f(x - vx, y - vy);
    }

    public Vector2f scaled(float s) {
        return new Vector2f(s * x, s * y);
    }

    public Vector2f inverse() {
        return new Vector2f(-x, -y);
    }

    public float length() {
        return (float) Math.hypot(x, y);
    }

    public Vector2f normalized() {
        float len = length();
        return new Vector2f(x / len, y / len);
    }

    public float euclideanDistance(Vector2f v) {
        return this.minus(v).length();
    }

    public boolean almostEquals(Vector2f v, float dx, float dy) {
        return differsAtMost(dx, x, v.x) && differsAtMost(dy, y, v.y);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (getClass() != other.getClass())
            return false;
        Vector2f v = (Vector2f) other;
        return Math.abs(v.x - x) <= EPSILON && Math.abs(v.y - y) <= EPSILON;
    }

    @Override
    public String toString() {
        return String.format("(%.2f,%.2f)", x, y);
    }
}