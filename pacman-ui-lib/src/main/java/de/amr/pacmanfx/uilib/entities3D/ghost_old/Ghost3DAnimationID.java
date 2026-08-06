/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old;


import de.amr.basics.Named;
import de.amr.pacmanfx.core.entities.ghost.Ghost;

import static java.util.Objects.requireNonNull;

public enum Ghost3DAnimationID implements Named {
    BRAKING, DRESS, FLASHING;

    public Ghost3DWrapperToBeRemoved.AnimationKey key(Ghost ghost) {
        requireNonNull(ghost);
        return new Ghost3DWrapperToBeRemoved.AnimationKey(this, ghost.personality());
    }
}
