/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.math;

import static de.amr.basics.math.Vector2f.vec2_float;

/**
 * A rectangle with short precision to save some bytes. Used to represent sprites and inner obstacle rectangles.
 *
 * @param x left-upper corner x
 * @param y left-upper corner y
 * @param width width of sprite
 * @param height height of sprite
 */
public record RectShort(short x, short y, short width, short height) {

    /** Sprite Zero, no sugar! */
    public static RectShort NULL_RECTANGLE = RectShort.sprite(0, 0, 0, 0);

    private static short checkNonNegativeShort(int value, String messageFormat) {
        if (value < 0 || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException(messageFormat.formatted(value));
        }
        return (short) value;
    }

    /**
     * @param x left upper corner x
     * @param y left upper corner y
     * @param width width in pixel
     * @param height height in pixel
     * @return a rectangle as used to define sprite regions
     */
    public static RectShort sprite(int x, int y, int width, int height) {
        return new RectShort(x, y, width, height);
    }

    public RectShort(int x, int y, int width, int height) {
        this(
            checkNonNegativeShort(x,      "Illegal x-position: %d"),
            checkNonNegativeShort(y,      "Illegal y-position: %d"),
            checkNonNegativeShort(width,  "Illegal width: %d"),
            checkNonNegativeShort(height, "Illegal height: %d"));
    }

    public RectShort(short x, short y, short width, short height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        checkNonNegativeShort(x + width,  "Maximum x-position out of range: %d");
        checkNonNegativeShort(y + height, "Maximum y-position out of range: %d");
    }

    public short xMax() { return (short) (x + width); }

    public short yMax() { return (short) (y + height); }

    public boolean contains(int x, int y) {
        return this.x <= x && x < xMax() &&  this.y <= y && y < yMax();
    }

    public Vector2f center() { return vec2_float(x + width * 0.5f, y + height * 0.5f); }
}