/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import javafx.scene.paint.Color;

public record PacConfig(
    Color headColor,
    Color eyesColor,
    Color palateColor,
    Color hairbowColor,
    Color hairBowPearlsColor,
    Color boobsColor,
    float size2D,
    float size3D)
{}
