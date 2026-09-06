/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class PacBoosterComp implements GameEntityComp {

    private boolean boosterEnabled;

    public boolean boosterEnabled() {
        return boosterEnabled;
    }

    public void setBoosterEnabled(boolean enabled) {
        this.boosterEnabled = enabled;
    }
}
