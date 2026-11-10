/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost.comp;

public record GhostAppearanceMaterialSet(
    Ghost3DPartsMaterials normalMaterials,
    Ghost3DPartsMaterials frightenedMaterials,
    Ghost3DPartsMaterials flashingMaterials
) {}
