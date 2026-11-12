/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost;

public record GhostView3D_MaterialSet(
    GhostView3D_PartsMaterials normalMaterials,
    GhostView3D_PartsMaterials frightenedMaterials,
    GhostView3D_PartsMaterials flashingMaterials
) {}
