/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities;

import de.amr.basics.Named;

public enum ActorAnimationID implements Named {
    PAC_MUNCHING,
    PAC_DYING,
    PAC_FULL,

    GHOST_EYES,
    GHOST_FLASHING,
    GHOST_FRIGHTENED,
    GHOST_NORMAL,
    GHOST_POINTS,

    // Pac-Man cut scenes
    BLINKY_DAMAGED,
    BLINKY_PATCHED,
    BLINKY_NAKED,

    // Ms. Pac-Man cut scenes
    MR_PAC_MAN_MUNCHING,
    BAG, JUNIOR,
    STORK_FLYING
}
