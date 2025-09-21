/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

/**
 * Immutable int 2D vector.
 *
 * @author Armin Reichert
 */
public record Vector2i(int x, int y) {

    public static final Vector2i ZERO = new Vector2i(0, 0);

    public static Vector2i of(int x, int y) {
        return new Vector2i(x, y);
    }

    /**
     * Access component by index
     *
     * @param i index (0 or 1)
     * @return first component if i == 0,  second component if i == 1
     */
    public int $(int i) {
        return switch (i) {
            case 0 -> x;
            case 1 -> y;
            default -> throw new IllegalArgumentException(String.format("Illegal index %d, only 0 or 1 are allowed", i));
        };
    }

    public Vector2i scaled(int s) {
        return new Vector2i(s * x, s * y);
    }

    public Vector2f scaled(double s) { return new Vector2f((float)s * x, (float)s * y); }

    public Vector2i inverse() { return new Vector2i(-x, -y); }

    public Vector2i plus(Vector2i v) {
        return new Vector2i(x + v.x, y + v.y);
    }

    public Vector2i plus(int dx, int dy) {
        return new Vector2i(x + dx, y + dy);
    }

    public Vector2f plus(float dx, float dy) {
        return new Vector2f(x + dx, y + dy);
    }

    public Vector2i minus(Vector2i v) {
        return new Vector2i(x - v.x, y - v.y);
    }

    public Vector2i minus(int dx, int dy) {
        return new Vector2i(x - dx, y - dy);
    }

    public Vector2f midpoint(Vector2i v) { return this.plus(v).scaled(0.5f); }

    public double euclideanDist(Vector2i v) {
        return Math.hypot(x - v.x, y - v.y);
    }

    public int dot(Vector2i v) { return x * v.x + y * v.y; }

    public boolean isOrthogonalTo(Vector2i v) { return dot(v) == 0; }

    public int manhattanDist(Vector2i v) {
        return Math.abs(x - v.x) + Math.abs(y - v.y);
    }

    public double length() { return euclideanDist(Vector2i.ZERO); }

    @Override
    public String toString() {
        return String.format("(%d,%d)", x, y);
    }

    public Vector2f toVector2f() {
        return new Vector2f(x, y);
    }
}