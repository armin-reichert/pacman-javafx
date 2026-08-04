/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost;

public record GhostMaterialSet(
    GhostComponentMaterialSet normalMaterial,
    GhostComponentMaterialSet frightenedMaterial,
    GhostComponentMaterialSet flashingMaterial) {}
