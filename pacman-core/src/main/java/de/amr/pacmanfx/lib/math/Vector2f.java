/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.math;

/**
 * Immutable 2D vector with float precision.
 */
public record Vector2f(float x, float y) {

    public static final float EPSILON = 1e-6f;

    public static final Vector2f ZERO = new Vector2f(0, 0);

    public static Vector2f of(double x, double y) {
        return new Vector2f((float) x, (float) y);
    }

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

    public Vector2f scaled(double s) {
        return new Vector2f((float)s * x, (float)s * y);
    }

    public Vector2f inverse() {
        return new Vector2f(-x, -y);
    }

    public float length() {
        return (float) Math.hypot(x, y);
    }

    public Vector2f normalize() {
        if (equals(ZERO)) {
            throw new IllegalArgumentException("Null vector cannot be normalized");
        }
        float len = length();
        return new Vector2f(x / len, y / len);
    }

    public Vector2f midpoint(Vector2f v) {
        return this.plus(v).scaled(0.5f);
    }

    public float euclideanDist(Vector2f v) {
        return this.minus(v).length();
    }

    public float manhattanDist(Vector2f v) {
        return Math.abs(x - v.x) + Math.abs(y - v.y);
    }

    /**
     * @param v other vector
     * @param dx maximum allowed deviation of x component
     * @param dy maximum allowed deviation of y component
     * @return {@code true} if this vector differs from the given vector at most as much as the given deviations
     */
    public boolean roughlyEquals(Vector2f v, float dx, float dy) {
        return Math.abs(v.x - x) <= dx && Math.abs(v.y - y) <= dy;
    }

    @Override
    public String toString() {
        return String.format("(%.2f,%.2f)", x, y);
    }
}