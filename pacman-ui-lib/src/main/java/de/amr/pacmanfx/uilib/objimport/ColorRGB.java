/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.objimport;

public record ColorRGB(float red, float green, float blue) {
    static ColorRGB BLACK = new ColorRGB(0, 0, 0);
}
