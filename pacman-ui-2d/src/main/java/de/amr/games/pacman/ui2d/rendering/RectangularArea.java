/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import javafx.geometry.Rectangle2D;

/**
 * A rectangular area to represent spritesheet regions.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of rectangle
 * @param height height of rectangle
 */
public record RectangularArea(short x, short y, short width, short height) {

    public static final RectangularArea EMPTY = new RectangularArea(0, 0, 0, 0);

    public static RectangularArea rect(int x, int y, int width, int height) {
        return new RectangularArea(x, y, width, height);
    }

    public RectangularArea(int x, int y, int width, int height) {
        this((short) x, (short) y, (short) width, (short) height);
    }

    public Rectangle2D toRect() {
        return new Rectangle2D(x, y, width, height);
    }
}