/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.common.Position;

public final class MovementSystem {

    public void moveAccelerated(Actor actor) {
        final Position position = actor.position();
        final Movement movement = actor.movement();

        position.add(movement.velX(), movement.velY());
        movement.add(movement.accX(), movement.accY());
    }

    public void setVelocity(Actor actor, float vx, float vy) {
        actor.movement().setVelocity(vx, vy);
    }

    public void setVelocityX(Actor actor, float vx) {
        actor.movement().setVelX(vx);
    }

    public void setVelocityY(Actor actor, float vy) {
        actor.movement().setVelY(vy);
    }

    public void setAcceleration(Actor actor, float ax, float ay) {
        actor.movement().setAcceleration(ax, ay);
    }
}
