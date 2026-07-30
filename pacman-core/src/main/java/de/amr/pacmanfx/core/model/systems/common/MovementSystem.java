/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.actors.GameEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.common.Position;

public final class MovementSystem {

    public void moveAccelerated(GameEntity actor) {
        final Position position = actor.position();
        actor.optComponent(Movement.class).ifPresent(movement -> {
            position.add(movement.velX(), movement.velY());
            movement.add(movement.accX(), movement.accY());
        });
    }

    public void setVelocity(GameEntity actor, float vx, float vy) {
        actor.optComponent(Movement.class).ifPresent(movement -> movement.setVelocity(vx, vy));
    }

    public void setVelocityX(GameEntity actor, float vx) {
        actor.optComponent(Movement.class).ifPresent(movement -> movement.setVelX(vx));
    }

    public void setVelocityY(GameEntity actor, float vy) {
        actor.optComponent(Movement.class).ifPresent(movement -> movement.setVelY(vy));
    }

    public void setAcceleration(GameEntity actor, float ax, float ay) {
        actor.optComponent(Movement.class).ifPresent(movement -> movement.setAcceleration(ax, ay));
    }
}
