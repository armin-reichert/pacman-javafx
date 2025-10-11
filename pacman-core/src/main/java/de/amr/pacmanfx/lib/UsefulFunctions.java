/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import de.amr.pacmanfx.Globals;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public interface UsefulFunctions {
    /**
     * @param position a position
     * @return tile containing given position
     */
    static Vector2i tileAt(Vector2f position) {
        requireNonNull(position);
        return tileAt(position.x(), position.y());
    }

    /**
     * @param x x position
     * @param y y position
     * @return tile containing given position
     */
    static Vector2i tileAt(float x, float y) {
        float tx = x >= 0 ? x / TS : (x - TS) / TS;
        float ty = y >= 0 ? y / TS : (y - TS) / TS;
        return Vector2i.of((int) tx, (int) ty);
    }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position  (scaled by tile size) half tile right of tile origin
     */
    static Vector2f halfTileRightOf(int tileX, int tileY) {
        return Vector2f.of(TS * tileX + Globals.HTS, TS * tileY);
    }

    static Vector2f halfTileRightOf(Vector2i tile) {
        return halfTileRightOf(tile.x(), tile.y());
    }

    static boolean isEven(int n) {
        return n % 2 == 0;
    }

    static boolean isOdd(int n) {
        return n % 2 != 0;
    }

    /**
     * @param value1 value1
     * @param value2 value2
     * @param t      "time" between 0 and 1
     * @return linear interpolation between {@code value1} and {@code value2} values
     */
    static double lerp(double value1, double value2, double t) {
        return (1 - t) * value1 + t * value2;
    }
}
