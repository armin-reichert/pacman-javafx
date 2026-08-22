/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimFacade;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;

import static java.util.Objects.requireNonNull;

public final class ActorSpriteAnimController {

    public ActorSpriteAnimController() {}

    public void setAnimations(GameEntity actor, SpriteAnimFacade animations) {
        requireNonNull(actor);
        requireNonNull(animations);
        actor.reqComp(SpriteAnimationComp.class).setSpriteAnimations(animations);
    }

    public boolean hasNoAnimations(GameEntity actor) {
        return spriteAnim(actor).isEmpty();
    }

    public Object animation(GameEntity actor, Named animationID) {
        return spriteAnim(actor).animation(animationID);
    }

    public void selectAndSetFrame(GameEntity actor, Named animationID, int frameIndex) {
        select(actor, animationID);
        setAnimationFrame(actor, animationID, frameIndex);
    }

    public Named selectedAnimationID(GameEntity actor) {
        return spriteAnim(actor).selectedAnimationID();
    }

    public boolean isSelected(GameEntity actor, Named animationID) {
        requireNonNull(animationID);
        final Named selectedID = selectedAnimationID(actor);
        return selectedID != null && animationID.hasSameNameAs(selectedAnimationID(actor));
    }

    public void setAnimationFrame(GameEntity actor, Named animationID, int frameIndex) {
        requireNonNull(animationID);
        spriteAnim(actor).setAnimationFrame(animationID, frameIndex);
    }

    public void select(GameEntity actor, Named animationID) {
        spriteAnim(actor).select(animationID);
    }

    public RectShort currentSprite(GameEntity actor) {
        return spriteAnim(actor).currentSprite();
    }

    public void advanceFrame(GameEntity actor) {
        final int currentFrame = currentFrame(actor);
        if (currentFrame < spriteAnim(actor).numFrames() - 1) {
            spriteAnim(actor).setAnimationFrame(selectedAnimationID(actor), currentFrame + 1);
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

    private SpriteAnimFacade spriteAnim(GameEntity actor) {
        requireNonNull(actor);
        return actor.reqComp(SpriteAnimationComp.class).spriteAnimations();
    }
}
