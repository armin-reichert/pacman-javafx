/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.GameEntityComp;
import de.amr.pacmanfx.core.steering.Steering;

public class SteeringComp<E extends GameEntity> implements GameEntityComp {

    private Steering<E> steering;

    public SteeringComp() {}

    @Override
    public void reset() {
        if (steering != null) {
            steering.init();
        }
    }

    public Steering<E> steering() {
        return steering;
    }

    public void setSteering(Steering<E> steering) {
        this.steering = steering;
    }
}
