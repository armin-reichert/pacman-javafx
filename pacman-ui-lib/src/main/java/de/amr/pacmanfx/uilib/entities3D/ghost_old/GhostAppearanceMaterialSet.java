/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old;

import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DMaterialSet;

public record GhostAppearanceMaterialSet(
    Ghost3DMaterialSet normal,
    Ghost3DMaterialSet frightened,
    Ghost3DMaterialSet flashing
) {}
