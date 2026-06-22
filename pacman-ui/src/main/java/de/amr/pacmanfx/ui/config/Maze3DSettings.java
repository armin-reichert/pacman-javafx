/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

public record Maze3DSettings(
    float obstacleBaseHeight,
    float obstacleCornerRadius,
    float obstacleOpacity,
    float obstacleWallThickness,
    String darkWallFillColor
) {}
