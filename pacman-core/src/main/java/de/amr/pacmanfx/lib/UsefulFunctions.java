/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import de.amr.pacmanfx.Globals;

import java.util.Random;

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
        return Vector2i.of((int) (x / Globals.TS), (int) (y / Globals.TS));
    }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position  (scaled by tile size) half tile right of tile origin
     */
    static Vector2f halfTileRightOf(int tileX, int tileY) {
        return Vector2f.of(Globals.TS * tileX + Globals.HTS, Globals.TS * tileY);
    }

    static Vector2f halfTileRightOf(Vector2i tile) {
        return halfTileRightOf(tile.x(), tile.y());
    }

    /**
     * @param tiles amount of tiles
     * @return pixels corresponding to amount of tiles
     */
    static float tiles_to_px(double tiles) {
        return (float) tiles * Globals.TS;
    }

    /**
     * @param min left interval bound
     * @param maxExclusive right (open) interval bound
     * @return Random integer number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged to
     * guarantee <code>a<=b</code>
     */
    static int randomInt(int min, int maxExclusive) {
        if (min > maxExclusive) {
            var tmp = min;
            min = maxExclusive;
            maxExclusive = tmp;
        }
        return min + new Random().nextInt(maxExclusive - min);
    }

    /**
     * @param min left interval bound
     * @param maxExclusive right (open) interval bound
     * @return Random floating-point number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged
     * to guarantee <code>a<=b</code>
     */
    static float randomFloat(float min, float maxExclusive) {
        if (min > maxExclusive) {
            var tmp = min;
            min = maxExclusive;
            maxExclusive = tmp;
        }
        return min + (maxExclusive - min) * new Random().nextFloat();
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
