package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.scene.canvas.Canvas;

public abstract class ActorSpriteRenderer extends BaseRenderer implements SpriteRenderer {

    protected ActorSpriteRenderer(Canvas canvas) {
        super(canvas);
    }

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     */
    public void drawActor(Actor actor) {
        actor.animations()
            .map(animations -> animations.currentSprite(actor))
            .ifPresent(sprite -> drawSpriteCentered(actor.center(), sprite));
    }
}
