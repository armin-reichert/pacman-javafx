/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.steering.Steering;

public class SteeringComp implements GameEntityComponent {

    private Steering steering;

    public SteeringComp() {}

    @Override
    public void reset() {
        if (steering != null) {
            steering.init();
        }
    }

    public Steering steering() {
        return steering;
    }

    public void setSteering(Steering steering) {
        this.steering = steering;
    }
}
