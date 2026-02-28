/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d.config;

public record Config3D(
    ActorConfig3D actor,
    EnergizerConfig3D energizer,
    FloorConfig3D floor,
    HouseConfig3D house,
    PelletConfig3D pellet
) {}
