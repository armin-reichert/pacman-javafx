/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.lib;

public record ColorRGBA(byte red, byte green, byte blue, double alpha) {

    public static final ColorRGBA BLACK = ColorRGBA.of(0, 0, 0, 1.0);

    public static ColorRGBA of(int red, int green, int blue, double alpha) {
        return new ColorRGBA((byte) red, (byte) green, (byte) blue, alpha);
    }
}
