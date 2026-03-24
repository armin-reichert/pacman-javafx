/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import de.amr.pacmanfx.uilib.model3D.actor.PacComponentColors;
import javafx.scene.paint.Color;

public record PacConfig(
    PacComponentColors colors,
    Color hairbowColor,
    Color hairBowPearlsColor,
    Color boobsColor,
    float size2D,
    float size3D)
{}
