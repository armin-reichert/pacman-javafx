/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.world;

import de.amr.basics.ecs.GameEntity;

public class Energizer extends GameEntity {

    public Energizer() {
        setComp(EnergizerStateComp.class, new EnergizerStateComp());
    }

    public EnergizerStateComp state() {
        return reqComp(EnergizerStateComp.class);
    }
}
