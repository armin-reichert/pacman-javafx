package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.model.actors.Actor;

public interface ActorRenderer extends CanvasRenderer {

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     */
    void drawActor(Actor actor);
}
