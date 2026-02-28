/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d.config;

public record Config3D(
    ActorConfig actor,
    EnergizerConfig energizer,
    FloorConfig floor,
    HouseConfig house,
    PelletConfig pellet
) {}
