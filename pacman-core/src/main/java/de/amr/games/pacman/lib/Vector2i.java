/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Immutable int 2D vector.
 *
 * @author Armin Reichert
 */
public final class Vector2i {

    public static final Vector2i ZERO = new Vector2i(0, 0);

    private final int x;
    private final int y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Vector2i scaled(int s) {
        return new Vector2i(s * x, s * y);
    }

    public Vector2i plus(Vector2i v) {
        return new Vector2i(x + v.x, y + v.y);
    }

    public Vector2i plus(int dx, int dy) {
        return new Vector2i(x + dx, y + dy);
    }

    public Vector2i minus(Vector2i v) {
        return new Vector2i(x - v.x, y - v.y);
    }

    public Vector2i minus(int dx, int dy) {
        return new Vector2i(x - dx, y - dy);
    }

    public float euclideanDistance(Vector2i v) {
        return (float) Math.hypot(x - v.x, y - v.y);
    }

    public float manhattanDistance(Vector2i v) {
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

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector2i other = (Vector2i) obj;
        return x == other.x && y == other.y;
    }
}