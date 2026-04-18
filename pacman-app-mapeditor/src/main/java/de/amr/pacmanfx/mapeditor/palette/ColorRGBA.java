/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.mapeditor.palette;

public record ColorRGBA(byte red, byte green, byte blue, double alpha) {

    public static final ColorRGBA BLACK = ColorRGBA.of(0, 0, 0, 1.0);

    public static ColorRGBA of(int red, int green, int blue, double alpha) {
        return new ColorRGBA((byte) red, (byte) green, (byte) blue, alpha);
    }

    private static void validateColorValue(int value, String message) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Illegal %s: %d".formatted(message, value));
        }
    }

    public ColorRGBA {
        validateColorValue(red, "red");
        validateColorValue(green, "green");
        validateColorValue(blue, "blue");
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Illegal alpha: %f".formatted(alpha));
        }
    }
}
