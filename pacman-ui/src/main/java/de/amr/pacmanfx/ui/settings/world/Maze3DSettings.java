/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.settings.world;

import javafx.scene.paint.Color;

public record Maze3DSettings(
    Color floorColor,
    Color lightColor,
    double wallHeight,
    double wallOpacity,
    float obstacleWallThickness,
    String darkWallFillColor
) {}
