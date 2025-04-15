/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman;

import de.amr.games.pacman.controller.CoinMechanism;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventManager;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameException;
import org.tinylog.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import static java.util.Objects.requireNonNull;

/**
 * Global is not evil.
 *
 * @see <a href="https://www.youtube.com/watch?v=ogHl_OwcZWE">this video</a>
 */
public interface Globals {

    CoinMechanism    THE_COIN_MECHANISM = new CoinMechanism();
    GameController   THE_GAME_CONTROLLER = new GameController();
    GameEventManager THE_GAME_EVENT_MANAGER = new GameEventManager();
    Random           THE_RNG = new Random();

    byte TICKS_PER_SECOND = 60;

    /** Tile size (8px). */
    int TS = 8;

    /** Half tile size (4px). */
    int HTS = 4;

    /**
     * Directory under which application stores high scores, maps etc. (default: <code>&lt;user_home/.pacmanfx&gt;</code>).
     */
    File HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_directory&gt;/maps</code>).
     */
    File CUSTOM_MAP_DIR = new File(HOME_DIR, "maps");

    static void checkDirectories() {
        String homeDirDesc = "Pac-Man FX home directory";
        String customMapDirDesc = "Pac-Man FX custom map directory";
        boolean success = ensureDirectoryExistsAndIsWritable(HOME_DIR, homeDirDesc);
        if (success) {
            Logger.info(homeDirDesc + " is " + HOME_DIR);
            success = ensureDirectoryExistsAndIsWritable(CUSTOM_MAP_DIR, customMapDirDesc);
            if (success) {
                Logger.info(customMapDirDesc + " is " + CUSTOM_MAP_DIR);
            }
            Logger.info("Directory check passed!");
        }
    }

    private static boolean ensureDirectoryExistsAndIsWritable(File dir, String description) {
        requireNonNull(dir);
        if (!dir.exists()) {
            Logger.info(description + " does not exist, create it...");
            if (!dir.mkdirs()) {
                Logger.error(description + " could not be created");
                return false;
            }
            Logger.error(description + " has been created");
            if (!dir.canWrite()) {
                Logger.error(description + " is not writeable");
                return false;
            }
        }
        return true;
    }

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
        return Vector2i.of((int) (x / TS), (int) (y / TS));
    }

    /**
     * @param tileX tile x coordinate
     * @param tileY tile y coordinate
     * @return position  (scaled by tile size) half tile right of tile origin
     */
    static Vector2f halfTileRightOf(int tileX, int tileY) {
        return Vector2f.of(TS * tileX + HTS, TS * tileY);
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
        return lowerBound + THE_RNG.nextInt(upperBoundExclusive - lowerBound);
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
        return a + (b - a) * THE_RNG.nextFloat();
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
        return a + (b - a) * THE_RNG.nextDouble();
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