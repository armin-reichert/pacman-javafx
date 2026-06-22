/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;

import java.util.List;

public record WorldConfig(
    PacConfig pac,
    List<GhostConfig> ghosts,
    BonusConfig bonus,
    LevelCounterConfig3D levelCounter,
    LivesCounterConfig3D livesCounter,
    MazeConfig3D maze,
    HouseConfig3D house,
    FloorConfig3D floor,
    PelletConfig3D pellet,
    EnergizerConfig3D energizer)
{}
