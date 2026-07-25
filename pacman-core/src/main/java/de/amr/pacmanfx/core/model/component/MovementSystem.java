package de.amr.pacmanfx.core.model.component;

import de.amr.pacmanfx.core.model.actors.Actor;

public class MovementSystem {

    public void moveAccelerated(Actor actor) {
        actor.position.x += actor.movement.velX;
        actor.position.y += actor.movement.velY;
        actor.movement.velX += actor.movement.accX;
        actor.movement.velY += actor.movement.accY;
    }
}
