/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import org.tinylog.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Armin Reichert
 */
public class Globals {

    /**
     * Tile size (8px).
     */
    public static final int TS = 8;

    /**
     * Half tile size (4px).
     */
    public static final int HTS = 4;

    public static final Random RND = new Random();

    private static final String MSG_TILE_NULL = "Tile must not be null";
    private static final String MSG_DIR_NULL = "Direction must not be null";

    public static Vector2i v2i(int x, int y) {
        return new Vector2i(x, y);
    }

    public static Vector2f v2f(double x, double y) {
        return new Vector2f((float) x, (float) y);
    }

    /**
     * @param position a position
     * @return tile containing given position
     */
    public static Vector2i tileAt(Vector2f position) {
        checkNotNull(position);
        return tileAt(position.x(), position.y());
    }

    /**
     * @param x x position
     * @param y y position
     * @return tile containing given position
     */
    public static Vector2i tileAt(float x, float y) {
        return v2i((int) (x / TS), (int) (y / TS));
    }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position  (scaled by tile size) half tile right of tile origin
     */
    public static Vector2f halfTileRightOf(int tileX, int tileY) {
        return v2f(TS * tileX + HTS, TS * tileY);
    }

    public static Vector2f halfTileRightOf(Vector2i tile) {
        return halfTileRightOf(tile.x(), tile.y());
    }

    public static float t(double tiles) {
        return (float) tiles * TS;
    }

    public static <T> T checkNotNull(T value) {
        return Objects.requireNonNull(value, "");
    }

    public static <T> T checkNotNull(T value, String message) {
        return Objects.requireNonNull(value, message);
    }

    public static Vector2i checkTileNotNull(Vector2i tile) {
        return checkNotNull(tile, MSG_TILE_NULL);
    }

    public static Direction checkDirectionNotNull(Direction dir) {
        return checkNotNull(dir, MSG_DIR_NULL);
    }

    public static double requirePositive(double value, String messageFormat) {
        if (value < 0) {
            throw new IllegalArgumentException(String.format(messageFormat, value));
        }
        return value;
    }

    public static double requirePositive(double value) {
        return requirePositive(value, "%f must be positive");
    }

    /**
     * @param lowerBound left interval bound
     * @param upperBoundExclusive right interval bound
     * @return Random integer number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged to
     * guarantee <code>a<=b</code>
     */
    public static int randomInt(int lowerBound, int upperBoundExclusive) {
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
    public static float randomFloat(float a, float b) {
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
    public static double randomDouble(double a, double b) {
        if (a > b) {
            var tmp = a;
            a = b;
            b = tmp;
        }
        return a + (b - a) * RND.nextDouble();
    }

    public static boolean inPercentOfCases(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Percent value must be in range [0, 100] but is %d", percent));
        }
        if (percent == 0) {
            return false;
        }
        if (percent == 100) {
            return true;
        }
        return randomInt(0, 100) < percent;
    }

    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    public static boolean isOdd(int n) {
        return n % 2 != 0;
    }

    /**
     * @param value some value
     * @param from lower bound (inclusive)
     * @param to upper bound (inclusive)
     * @return {@code true} if value is in closed interval {@code [from; to]}
     */
    public static boolean inClosedRange(long value, long from, long to) {
        return from <= value && value <= to;
    }

    /**
     * @param value1 value1
     * @param value2 value2
     * @param t      "time" between 0 and 1
     * @return linear interpolation between {@code value1} and {@code value2} values
     */
    public static double lerp(double value1, double value2, double t) {
        return (1 - t) * value1 + t * value2;
    }

    /**
     * @param value some value
     * @param min   lower bound of interval
     * @param max   upper bound of interval
     * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
     * larger
     */
    public static float clamp(float value, float min, float max) {
        return (value < min) ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return (value < min) ? min : Math.min(value, max);
    }

    /**
     * @param value some value
     * @param min   lower bound of interval
     * @param max   upper bound of interval
     * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
     * larger
     */
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * @param delta  maximum allowed deviation (non-negative number)
     * @param value  value
     * @param target target value
     * @return {@code true} if the given values differ at most by the given difference
     */
    public static boolean differsAtMost(double delta, double value, double target) {
        if (delta < 0) {
            throw new IllegalArgumentException(String.format("Difference must not be negative but is %f", delta));
        }
        return value >= (target - delta) && value <= (target + delta);
    }

    public static byte[][] copyByteArray2D(byte[][] array) {
        return Arrays.stream(array).map(byte[]::clone).toArray(byte[][]::new);
    }

    @SafeVarargs
    public static <T> boolean oneOf(T value, T... alternatives) {
        if (value == null) {
            return false;
        }
        return switch (alternatives.length) {
            case 0 -> false;
            case 1 -> value.equals(alternatives[0]);
            default -> Arrays.asList(alternatives).contains(value);
        };
    }

    /**
     * @param colorSpec color specification in one of the following 3 formats:
     *                  <ul>
     *                  <li><code>#rrggbb</code> (hexadecimal digits)</li>
     *                  <li><code>rrggbb</code>(hexadecimal digits)</li>
     *                  <li><code>rgb(red,green,blue)</code> (decimal values from 0.255)</li>
     *                  </ul>
     * @return color spec in the form <code>rrggbb</code> (hexadecimal digits)
     */
    public static Optional<String> colorToHexFormat(String colorSpec) {
        if (Pattern.matches("#?\\p{XDigit}{6}", colorSpec)) {
            return Optional.of(colorSpec);
        }
        Pattern rgbPattern = Pattern.compile("rgb\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)");
        Matcher m = rgbPattern.matcher(colorSpec);
        if (m.matches()) {
            String r = m.group(1), g = m.group(2), b = m.group(3);
            Short red = parseColorComponent(colorSpec, r);
            Short green = parseColorComponent(colorSpec, g);
            Short blue = parseColorComponent(colorSpec, b);
            if (red == null || green == null || blue == null) {
                return Optional.empty();
            }
            HexFormat fmt = HexFormat.of();
            return Optional.of(fmt.toHexDigits(red).substring(2)
                + fmt.toHexDigits(green).substring(2)
                + fmt.toHexDigits(blue).substring(2));
        } else {
            Logger.error("'{}': Invalid color format", colorSpec);
        }
        return Optional.empty();
    }

    private static Short parseColorComponent(String color, String comp) {
        try {
            short value = Short.parseShort(comp);
            if (!inClosedRange(value, 0, 255)) {
                Logger.error("'{}': Component value {} is out of range 0.255", color, value);
                return null;
            }
            return value;
        } catch (NumberFormatException x) {
            Logger.error("No valid color component value: '{}'", comp);
            return null;
        }
    }
}