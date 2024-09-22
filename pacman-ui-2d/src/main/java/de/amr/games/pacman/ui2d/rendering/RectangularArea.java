/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

/**
 * A rectangular area with short precision to represent sprite sheet regions.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of rectangle
 * @param height height of rectangle
 */
public record RectangularArea(short x, short y, short width, short height) {

    public static final RectangularArea PIXEL = new RectangularArea(0, 0, 1, 1);

    public static RectangularArea rect(int x, int y, int width, int height) {
        return new RectangularArea(x, y, width, height);
    }

    public RectangularArea(int x, int y, int width, int height) {
        this((short) x, (short) y, (short) width, (short) height);
    }
}