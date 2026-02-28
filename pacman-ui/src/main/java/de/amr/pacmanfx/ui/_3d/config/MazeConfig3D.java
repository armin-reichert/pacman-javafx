/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d.config;

public record MazeConfig3D(
    float obstacleBaseHeight,
    float obstacleCornerRadius,
    float obstacleOpacity,
    float obstacleWallThickness,
    String darkWallFillColor
) {}
