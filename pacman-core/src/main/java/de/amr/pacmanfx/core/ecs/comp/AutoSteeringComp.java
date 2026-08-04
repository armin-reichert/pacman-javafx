package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.steering.Steering;

public class AutoSteeringComp implements GameEntityComponent {
    private Steering steering;

    public AutoSteeringComp() {
    }

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
