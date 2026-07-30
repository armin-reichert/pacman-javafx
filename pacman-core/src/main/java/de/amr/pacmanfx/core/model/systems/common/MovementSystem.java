/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.common.PositionComponent;

public final class MovementSystem {

    public void moveAccelerated(GameEntity actor) {
        final PositionComponent position = actor.position();
        actor.optComponent(MovementComponent.class).ifPresent(movement -> {
            position.add(movement.velX(), movement.velY());
            movement.add(movement.accX(), movement.accY());
        });
    }

    public void setVelocity(GameEntity actor, float vx, float vy) {
        actor.optComponent(MovementComponent.class).ifPresent(movement -> movement.setVelocity(vx, vy));
    }

    public void setVelocityX(GameEntity actor, float vx) {
        actor.optComponent(MovementComponent.class).ifPresent(movement -> movement.setVelX(vx));
    }

    public void setVelocityY(GameEntity actor, float vy) {
        actor.optComponent(MovementComponent.class).ifPresent(movement -> movement.setVelY(vy));
    }

    public void setAcceleration(GameEntity actor, float ax, float ay) {
        actor.optComponent(MovementComponent.class).ifPresent(movement -> movement.setAcceleration(ax, ay));
    }
}
