/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config.world;

public record Maze3DSettings(
    float obstacleBaseHeight,
    float obstacleCornerRadius,
    float obstacleOpacity,
    float obstacleWallThickness,
    String darkWallFillColor
) {}
