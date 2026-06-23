/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

public record PacSettings(
    PacColors colors,
    MsPacManComponentColors msColors,
    float size3D)
{
    public PacSettings withModifiedSize3D(float newSize) {
        return new PacSettings(colors, msColors, newSize);
    }
}
