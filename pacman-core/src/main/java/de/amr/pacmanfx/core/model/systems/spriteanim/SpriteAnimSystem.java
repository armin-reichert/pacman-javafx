/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.spriteanim;


import de.amr.basics.Identifier;
import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnimComponent;

import static java.util.Objects.requireNonNull;

public class SpriteAnimSystem {

    public void setAnimations(GameEntity actor, SpriteAnimationAccess animations) {
        actor.requireComponent(SpriteAnimComponent.class).setAnimations(animations);
    }

    private SpriteAnimationAccess spriteAnim(GameEntity actor) {
        return actor.requireComponent(SpriteAnimComponent.class).delegate();
    }

    public boolean hasNoAnimations(GameEntity actor) {
        return spriteAnim(actor).isEmpty();
    }

    public Object animation(GameEntity actor, Identifier animationID) {
        return spriteAnim(actor).animation(animationID);
    }

    public void selectAndSetFrame(GameEntity actor, Identifier animationID, int frameIndex) {
        select(actor, animationID);
        setAnimationFrame(actor, animationID, frameIndex);
    }

    public Identifier selectedAnimationID(GameEntity actor) {
        return spriteAnim(actor).selectedAnimationID();
    }

    public boolean isSelected(GameEntity actor, Identifier animationID) {
        requireNonNull(animationID);
        final Identifier selectedID = selectedAnimationID(actor);
        return selectedID != null && animationID.identifies(selectedAnimationID(actor));
    }

    public void setAnimationFrame(GameEntity actor, Identifier animationID, int frameIndex) {
        spriteAnim(actor).setAnimationFrame(animationID, frameIndex);
    }

    public void select(GameEntity actor, Identifier animationID) {
        spriteAnim(actor).select(animationID);
    }

    public RectShort currentSprite(GameEntity actor) {
        return spriteAnim(actor).currentSprite();
    }

    public void advanceFrame(GameEntity actor) {
        final int currentFrame = currentFrame(actor);
        if (currentFrame < spriteAnim(actor).numFrames()) {

        }
    }

    public int currentFrame(GameEntity actor) {
        return spriteAnim(actor).currentFrame();
    }

    public void playSelected(GameEntity actor) {
        spriteAnim(actor).playSelected();
    }

    public void stopSelected(GameEntity actor) {
        spriteAnim(actor).stopSelected();
    }

    public void resetSelected(GameEntity actor) {
        spriteAnim(actor).resetSelected();
    }
}
