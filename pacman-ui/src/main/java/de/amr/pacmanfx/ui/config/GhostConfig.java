/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import javafx.scene.paint.Color;

public record GhostConfig(
    float size2D,
    float size3D,
    Color dressColor,
    Color eyeballColor,
    Color pupilsColor
) {}
