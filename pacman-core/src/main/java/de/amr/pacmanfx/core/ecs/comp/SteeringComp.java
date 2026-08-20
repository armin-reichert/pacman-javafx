/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.steering.Steering;

public class SteeringComp<E extends GameEntity> implements EntityComponent {

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
