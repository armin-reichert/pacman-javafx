/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.comp;

public record PacSettings(
    PacColors colors,
    FemaleBodyPartsColors msColors,
    float size3D)
{
    public PacSettings resized(float newSize) {
        return new PacSettings(colors, msColors, newSize);
    }
}
