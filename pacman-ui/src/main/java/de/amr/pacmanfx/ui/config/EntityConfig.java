/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import java.util.List;

public record EntityConfig(
    PacConfig pacConfig,
    List<GhostConfig> ghostConfigs,
    BonusConfig bonusConfig,
    EnergizerConfig3D energizer,
    FloorConfig3D floor,
    HouseConfig3D house,
    LevelCounterConfig3D levelCounter,
    LivesCounterConfig3D livesCounter,
    MazeConfig3D maze,
    PelletConfig3D pellet) {}
