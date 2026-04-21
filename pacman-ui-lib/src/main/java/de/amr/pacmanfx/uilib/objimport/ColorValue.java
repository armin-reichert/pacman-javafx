/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

public record ColorValue(double red, double green, double blue) {
    static ColorValue BLACK = new ColorValue(0.0, 0.0, 0.0);
}
