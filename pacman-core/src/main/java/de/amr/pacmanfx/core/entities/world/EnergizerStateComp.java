/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.world;

import de.amr.basics.ecs.GameEntityComp;

public class EnergizerStateComp implements GameEntityComp {

    private boolean on;

    public boolean on() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
