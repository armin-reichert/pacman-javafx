/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost.comp;

public record GhostView3DMaterialSet(
    GhostView3DPartsMaterials normalMaterials,
    GhostView3DPartsMaterials frightenedMaterials,
    GhostView3DPartsMaterials flashingMaterials
) {}
