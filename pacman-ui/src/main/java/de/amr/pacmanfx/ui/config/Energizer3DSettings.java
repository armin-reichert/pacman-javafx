/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

public record Energizer3DSettings(
    int pumpingFrequency,
    float radius,
    float floorElevation,
    float scalingInflated,
    float scalingExpanded
) {}
