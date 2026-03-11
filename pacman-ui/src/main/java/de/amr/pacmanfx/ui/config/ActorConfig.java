/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

public record ActorConfig(
    PacConfig pacConfig,
    float ghostSize,
    float bonusSymbolWidth,
    float bonusPointsWidth
) {}
