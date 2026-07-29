/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.spriteanim;


import de.amr.basics.Identifier;
import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;

import static java.util.Objects.requireNonNull;

public class SpriteAnimSystem {

    public void setAnimations(Actor actor, SpriteAnimationAccess animations) {
        actor.assertComponent(SpriteAnim.class).setAnimations(animations);
    }

    private SpriteAnimationAccess spriteAnim(Actor actor) {
        return actor.assertComponent(SpriteAnim.class).delegate();
    }

    public boolean hasNoAnimations(Actor actor) {
        return spriteAnim(actor).isEmpty();
    }

    public Object animation(Actor actor, Identifier animationID) {
        return spriteAnim(actor).animation(animationID);
    }

    public void selectAndSetFrame(Actor actor, Identifier animationID, int frameIndex) {
        select(actor, animationID);
        setAnimationFrame(actor, animationID, frameIndex);
    }

    public Identifier selectedAnimationID(Actor actor) {
        return spriteAnim(actor).selectedAnimationID();
    }

    public boolean isSelected(Actor actor, Identifier animationID) {
        requireNonNull(animationID);
        final Identifier selectedID = selectedAnimationID(actor);
        return selectedID != null && animationID.identifies(selectedAnimationID(actor));
    }

    public void setAnimationFrame(Actor actor, Identifier animationID, int frameIndex) {
        spriteAnim(actor).setAnimationFrame(animationID, frameIndex);
    }

    public void select(Actor actor, Identifier animationID) {
        spriteAnim(actor).select(animationID);
    }

    public RectShort currentSprite(Actor actor) {
        return spriteAnim(actor).currentSprite();
    }

    public void advanceFrame(Actor actor) {
        final int currentFrame = currentFrame(actor);
        if (currentFrame < spriteAnim(actor).numFrames()) {

        }
    }

    public int currentFrame(Actor actor) {
        return spriteAnim(actor).currentFrame();
    }

    public void playSelected(Actor actor) {
        spriteAnim(actor).playSelected();
    }

    public void stopSelected(Actor actor) {
        spriteAnim(actor).stopSelected();
    }

    public void resetSelected(Actor actor) {
        spriteAnim(actor).resetSelected();
    }
}
