/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

/**
 * @author Armin Reichert
 */
public record NavPoint(int x, int y, Direction dir) {

    public static NavPoint np(int x, int y, Direction dir) {
        return new NavPoint(x, y, dir);
    }

    public static NavPoint np(Vector2i tile, Direction dir) {
        return np(tile.x(), tile.y(), dir);
    }

    public static NavPoint np(Vector2i tile) {
        return np(tile.x(), tile.y(), null);
    }

    public static NavPoint np(int x, int y) {
        return np(x, y, null);
    }

    @Override
    public String toString() {
        return "NavPoint{" +
            "x=" + x +
            ", y=" + y +
            (dir != null ? ", dir=" + dir : "") +
            '}';
    }

    public Vector2i tile() {
        return new Vector2i(x, y);
    }
}