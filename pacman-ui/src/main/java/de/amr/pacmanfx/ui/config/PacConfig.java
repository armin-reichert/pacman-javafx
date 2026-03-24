/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import de.amr.pacmanfx.uilib.model3D.actor.MsPacManComponentColors;
import de.amr.pacmanfx.uilib.model3D.actor.PacComponentColors;

public record PacConfig(
    PacComponentColors colors,
    MsPacManComponentColors msColors,
    float size2D,
    float size3D)
{}
