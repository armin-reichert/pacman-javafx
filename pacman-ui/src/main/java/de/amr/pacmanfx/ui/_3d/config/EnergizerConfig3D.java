/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d.config;

public record EnergizerConfig3D(
    int pumpingFrequency,
    float radius,
    float floorElevation,
    float scalingInflated,
    float scalingExpanded
) {}
