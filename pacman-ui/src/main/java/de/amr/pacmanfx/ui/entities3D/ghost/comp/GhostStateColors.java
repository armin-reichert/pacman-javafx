/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.entities3D.ghost.comp;

public record GhostStateColors(
    GhostComponentColors normal,
    GhostComponentColors frightened,
    GhostComponentColors flashing)
{}
