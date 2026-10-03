/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.world;

import de.amr.basics.ecs.GameEntity;

public class Door extends GameEntity {

    public Door() {
        setComponent(DoorDataComp.class, new DoorDataComp());
    }
}
