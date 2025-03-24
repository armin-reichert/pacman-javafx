/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import static de.amr.games.pacman.Globals.vec_2f;

/**
 * A rectangular area with short precision to represent sprite sheet regions.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of rectangle
 * @param height height of rectangle
 */
public record RectArea(short x, short y, short width, short height) {

    public static final RectArea PIXEL = new RectArea(0, 0, 1, 1);

    public static RectArea rect(int x, int y, int width, int height) {
        return new RectArea(x, y, width, height);
    }

    public RectArea(int x, int y, int width, int height) {
        this((short) x, (short) y, (short) width, (short) height);
    }

    public boolean contains(int x, int y) {
        return this.x <= x && x < this.x + width &&  this.y <= y && y < this.y + height;
    }

    public boolean contains(RectArea other) {
        return contains(other.x(), other.y()) && contains(other.x() + other.width(), other.y() + other.height());
    }

    public Vector2f center() { return vec_2f(x + width * 0.5f, y + height * 0.5f); }
}