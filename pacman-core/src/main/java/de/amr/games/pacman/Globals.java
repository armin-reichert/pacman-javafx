/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman;

import de.amr.games.pacman.controller.CoinStore;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameException;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Global is not evil.
 *
 * @see <a href="https://www.youtube.com/watch?v=ogHl_OwcZWE">this video</a>
 */
public interface Globals {

    CoinStore THE_COIN_STORE = new CoinStore();
    GameController THE_GAME_CONTROLLER = new GameController();

    byte TICKS_PER_SECOND = 60;

    /** Tile size (8px). */
    int TS = 8;

    /** Half tile size (4px). */
    int HTS = 4;

    Random RND = new Random();

    static Vector2i vec_2i(int x, int y) {
        return new Vector2i(x, y);
    }

    static Vector2f vec_2f(double x, double y) {
        return new Vector2f((float) x, (float) y);
    }

    /**
     * @param position a position
     * @return tile containing given position
     */
    static Vector2i tileAt(Vector2f position) {
        assertNotNull(position);
        return tileAt(position.x(), position.y());
    }

    /**
     * @param x x position
     * @param y y position
     * @return tile containing given position
     */
    static Vector2i tileAt(float x, float y) {
        return vec_2i((int) (x / TS), (int) (y / TS));
    }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position  (scaled by tile size) half tile right of tile origin
     */
    static Vector2f halfTileRightOf(int tileX, int tileY) {
        return vec_2f(TS * tileX + HTS, TS * tileY);
    }

    static Vector2f halfTileRightOf(Vector2i tile) {
        return halfTileRightOf(tile.x(), tile.y());
    }

    /**
     * @param tiles amount of tiles
     * @return pixels corresponding to amount of tiles
     */
    static float tiles_to_px(double tiles) {
        return (float) tiles * TS;
    }

    static <T> T assertNotNull(T value) {
        return Objects.requireNonNull(value, "");
    }

    static <T> T assertNotNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }

    static Vector2i assertTileNotNull(Vector2i tile) {
        return assertNotNull(tile, "Tile must not be null");
    }

    static byte assertValidGhostID(byte id) {
        if (id < 0 || id > 3) {
            throw GameException.invalidGhostID(id);
        }
        return id;
    }

    static int assertValidLevelNumber(int number) {
        if (number < 1)
            throw GameException.invalidLevelNumber(number);
        return number;
    }

    static Direction assertDirectionNotNull(Direction dir) {
        return assertNotNull(dir, "Direction must not be null");
    }

    static double assertNonNegative(double value, String messageFormat) {
        if (value < 0) {
            throw new IllegalArgumentException(String.format(messageFormat, value));
        }
        return value;
    }

    static double assertNonNegative(double value) {
        return assertNonNegative(value, "%f must be zero or positive");
    }

    /**
     * @param lowerBound left interval bound
     * @param upperBoundExclusive right interval bound
     * @return Random integer number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged to
     * guarantee <code>a<=b</code>
     */
    static int randomInt(int lowerBound, int upperBoundExclusive) {
        if (lowerBound > upperBoundExclusive) {
            var tmp = lowerBound;
            lowerBound = upperBoundExclusive;
            upperBoundExclusive = tmp;
        }
        return lowerBound + RND.nextInt(upperBoundExclusive - lowerBound);
    }

    /**
     * @param a left interval bound
     * @param b right interval bound
     * @return Random floating-point number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged
     * to guarantee <code>a<=b</code>
     */
    static float randomFloat(float a, float b) {
        if (a > b) {
            var tmp = a;
            a = b;
            b = tmp;
        }
        return a + (b - a) * RND.nextFloat();
    }

    /**
     * @param a left interval bound
     * @param b right interval bound
     * @return Random double-precision floating-point number from right-open interval <code>[a; b[</code>. Interval bounds
     * are rearranged to guarantee <code>a<=b</code>
     */
    static double randomDouble(double a, double b) {
        if (a > b) {
            var tmp = a;
            a = b;
            b = tmp;
        }
        return a + (b - a) * RND.nextDouble();
    }

    static boolean isEven(int n) {
        return n % 2 == 0;
    }

    /**
     * @param value some value
     * @param from lower bound (inclusive)
     * @param to upper bound (inclusive)
     * @return {@code true} if value is in closed interval {@code [from; to]}
     */
    static boolean inClosedRange(long value, long from, long to) {
        return from <= value && value <= to;
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

    /**
     * @param value some value
     * @param min   lower bound of interval
     * @param max   upper bound of interval
     * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
     * larger
     */
    static double clamp(double value, double min, double max) {
        return (value < min) ? min : Math.min(value, max);
    }

    /**
     * @param delta  maximum allowed deviation (non-negative number)
     * @param value  value
     * @param target target value
     * @return {@code true} if the given values differ at most by the given difference
     */
    static boolean differsAtMost(double delta, double value, double target) {
        if (delta < 0) {
            throw new IllegalArgumentException(String.format("Difference must not be negative but is %f", delta));
        }
        return value >= (target - delta) && value <= (target + delta);
    }

    static byte[][] copyArray2D(byte[][] array) {
        return Arrays.stream(array).map(byte[]::clone).toArray(byte[][]::new);
    }

    @SafeVarargs
    static <T> boolean oneOf(T value, T... alternatives) {
        if (value == null) {
            return false;
        }
        return switch (alternatives.length) {
            case 0 -> false;
            case 1 -> value.equals(alternatives[0]);
            default -> Arrays.asList(alternatives).contains(value);
        };
    }
}