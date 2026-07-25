package de.amr.pacmanfx.core.model.component;

import de.amr.pacmanfx.core.model.actors.Actor;

public class MovementSystem {

    public void moveAccelerated(Actor actor) {
        final Position position = actor.position();
        final Movement movement = actor.movement();

        position.x += movement.velX;
        position.y += movement.velY;
        movement.velX += movement.accX;
        movement.velY += movement.accY;
    }
}
