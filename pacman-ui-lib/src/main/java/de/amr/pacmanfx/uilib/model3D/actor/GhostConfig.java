/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

public record GhostConfig(
    float size2D,
    float size3D,
    GhostComponentColors normalColors,
    GhostComponentColors frightenedColors,
    GhostComponentColors flashingColors)
{
    public GhostColorSet createGhostColorSet() {
        return new GhostColorSet(normalColors, frightenedColors, flashingColors);
    }
}
