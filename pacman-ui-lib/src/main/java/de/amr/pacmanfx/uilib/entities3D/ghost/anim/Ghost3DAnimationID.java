/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.anim;


import de.amr.basics.Named;
import de.amr.pacmanfx.core.entities.Ghost;

import static java.util.Objects.requireNonNull;

public enum Ghost3DAnimationID implements Named {
    BRAKING, DRESS, FLASHING;

    public Ghost3DAnimationKey key(Ghost ghost) {
        requireNonNull(ghost);
        return new Ghost3DAnimationKey(this, ghost.personality());
    }
}
