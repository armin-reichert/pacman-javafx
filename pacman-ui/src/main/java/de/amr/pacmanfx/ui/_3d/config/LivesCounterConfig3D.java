/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d.config;

import javafx.scene.paint.Color;

public record LivesCounterConfig3D(
    int capacity,
    Color pillarColor,
    Color plateColor,
    float shapeSize
) {}
