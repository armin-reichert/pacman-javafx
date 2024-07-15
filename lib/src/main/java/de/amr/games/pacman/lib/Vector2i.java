/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.util.stream.Stream;

/**
 * Immutable int 2D vector.
 *
 * @author Armin Reichert
 */
public record Vector2i(int x, int y) {

    public static final Vector2i ZERO = new Vector2i(0, 0);

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

    public Vector2f scaled(float s) {
        return new Vector2f(s * x, s * y);
    }

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

    public double euclideanDistance(Vector2i v) {
        return Math.hypot(x - v.x, y - v.y);
    }

    public int manhattanDistance(Vector2i v) {
        return Math.abs(x - v.x) + Math.abs(y - v.y);
    }

    public Stream<Vector2i> neighbors() {
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT).map(dir -> this.plus(dir.vector()));
    }

    @Override
    public String toString() {
        return String.format("(%2d,%2d)", x, y);
    }

    public Vector2f toFloatVec() {
        return new Vector2f(x, y);
    }
}