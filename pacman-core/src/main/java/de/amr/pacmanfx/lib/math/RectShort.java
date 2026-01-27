/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.math;

/**
 * A rectangular area with short integer precision.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of sprite
 * @param height height of sprite
 */
public record RectShort(short x, short y, short width, short height) {

    /** Sprite Zero, no sugar! */
    public static RectShort ZERO = new RectShort(0, 0, 0, 0);

    public static RectShort rect(int x, int y, int width, int height) {
        return new RectShort(x, y, width, height);
    }

    public RectShort(int x, int y, int width, int height) {
        this((short) x, (short) y, (short) width, (short) height);
    }

    public int xMax() { return x + width; }

    public int yMax() { return y + height; }

    public boolean contains(int x, int y) {
        return this.x <= x && x < xMax() &&  this.y <= y && y < yMax();
    }

    public Vector2f center() { return Vector2f.of(x + width * 0.5f, y + height * 0.5f); }
}