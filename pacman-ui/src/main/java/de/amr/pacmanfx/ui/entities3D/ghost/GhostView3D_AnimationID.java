/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;

import static java.util.Objects.requireNonNull;

public enum GhostView3D_AnimationID implements Named {
    BRAKING, DRESS_MOVEMENT, FLASHING;

    public GhostView3D_AnimationKey key(Ghost ghost) {
        requireNonNull(ghost);
        return new GhostView3D_AnimationKey(this, ghost.personality());
    }
}
