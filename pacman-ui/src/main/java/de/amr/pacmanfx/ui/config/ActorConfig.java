/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

public record ActorConfig(
    PacConfig pacConfig,
    GhostConfig ghostConfig,
    float bonusSymbolWidth,
    float bonusPointsWidth
) {}
