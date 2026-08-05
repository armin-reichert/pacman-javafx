/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.model.entities.pac.Pac;

public final class PacPowerSystem {

    public void update(Pac pac) {

    }

    public void reset(Pac pac) {
        pac.power().reset();
    }
}
