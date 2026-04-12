/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.math;

import static de.amr.pacmanfx.lib.math.Vector2f.vec2_float;

/**
 * A rectangle with short precision. Used to represent sprites and inner obstacle rectangles.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of sprite
 * @param height height of sprite
 */
public record RectShort(short x, short y, short width, short height) {

    /** Sprite Zero, no sugar! */
    public static RectShort NULL_RECTANGLE = RectShort.of(0, 0, 0, 0);

    private static short checkNonNegativeShort(int value, String messageFormat) {
        if (value < 0 || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException(messageFormat.formatted(value));
        }
        return (short) value;
    }

    public static RectShort of(int x, int y, int width, int height) {
        return new RectShort(
            checkNonNegativeShort(x,      "Illegal sprite x-position: %d"),
            checkNonNegativeShort(y,      "Illegal sprite y-position: %d"),
            checkNonNegativeShort(width,  "Illegal sprite width: %d"),
            checkNonNegativeShort(height, "Illegal sprite height: %d"));
    }

    public RectShort(short x, short y, short width, short height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        checkNonNegativeShort(x + width,  "Sprite max x-position out of range: %d");
        checkNonNegativeShort(y + height, "Sprite max y-position out of range: %d");
    }

    public short xMax() { return (short) (x + width); }

    public short yMax() { return (short) (y + height); }

    public boolean contains(int x, int y) {
        return this.x <= x && x < xMax() &&  this.y <= y && y < yMax();
    }

    public Vector2f center() { return vec2_float(x + width * 0.5f, y + height * 0.5f); }
}