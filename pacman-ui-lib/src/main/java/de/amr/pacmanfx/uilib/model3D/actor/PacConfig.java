/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

public record PacConfig(
    PacComponentColors colors,
    MsPacManComponentColors msColors,
    float size2D,
    float size3D)
{
    public PacConfig withModifiedSize3D(float newSize) {
        return new PacConfig(colors, msColors, size2D, newSize);
    }
}
