/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.GameEntity;

public class MovementSystem {

    public void move(GameEntity actor) {
        actor.optMovementComp().ifPresent(movement -> {
            actor.pos().add(movement.velocityX(), movement.velocityY());
            movement.addVelocity(movement.accelerationX(), movement.accelerationY());
        });
    }

    public void setVelocity(GameEntity actor, float vx, float vy) {
        actor.optMovementComp().ifPresent(movement -> movement.setVelocity(vx, vy));
    }

    public void setVelocityX(GameEntity actor, float vx) {
        actor.optMovementComp().ifPresent(movement -> movement.setVelocityX(vx));
    }

    public void setVelocityY(GameEntity actor, float vy) {
        actor.optMovementComp().ifPresent(movement -> movement.setVelocityY(vy));
    }

    public void setAcceleration(GameEntity actor, float ax, float ay) {
        actor.optMovementComp().ifPresent(movement -> movement.setAcceleration(ax, ay));
    }
}
