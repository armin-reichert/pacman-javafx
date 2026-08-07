/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old;

import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DMaterials;

public record GhostMaterialSet(
    Ghost3DMaterials normalMaterial,
    Ghost3DMaterials frightenedMaterial,
    Ghost3DMaterials flashingMaterial) {}
