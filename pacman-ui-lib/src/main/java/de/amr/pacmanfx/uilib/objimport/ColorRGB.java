/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

public record ColorRGB(double red, double green, double blue) {
    static ColorRGB BLACK = new ColorRGB(0.0, 0.0, 0.0);
}
