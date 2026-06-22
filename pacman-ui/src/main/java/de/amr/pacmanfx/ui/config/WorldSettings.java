/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;

import java.util.List;

public record WorldSettings(
    PacConfig pac,
    List<GhostConfig> ghosts,
    Bonus3DSettings bonus,
    LevelCounter3DSettings levelCounter,
    LivesCounter3DSettings livesCounter,
    Maze3DSettings maze,
    House3DSettings house,
    Floor3DSettings floor,
    Pellet3DSettings pellet,
    Energizer3DSettings energizer)
{}
