/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationManager<SID extends Enum<SID>> implements AnimationManager {

    protected final SpriteSheet<SID> spriteSheet;
    protected final Map<Object, SpriteAnimation> animationsByID = new HashMap<>();
    protected Object selectedID;

    public SpriteAnimationManager(SpriteSheet<SID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public SpriteSheet<SID> spriteSheet() { return spriteSheet; }

    // TODO: this is somewhat crude but currently the way to keep the sprites up-to-date with actor direction etc.
    protected void updateActorSprites(Actor actor) {}

    public Object selectedAnimationID() { return selectedID; }

    public boolean isCurrentAnimationID(Object animationID) {
        requireNonNull(animationID);
        return animationID.equals(selectedID);
    }

    @Override
    public RectShort currentSprite(Actor actor) {
        var currentAnimation = currentAnimation();
        if (currentAnimation == null) {
            return null;
        }
        updateActorSprites(actor);
        return currentAnimation.currentSprite();
    }

    @Override
    public SpriteAnimation animation(Object animationID) {
        if (!animationsByID.containsKey(animationID)) {
            SpriteAnimation spriteAnimation = createAnimation(animationID);
            animationsByID.put(animationID, spriteAnimation);
        }
        return animationsByID.get(animationID);
    }

    protected SpriteAnimation createAnimation(Object animationID) {
        throw new UnsupportedOperationException("No idea how to create animation with ID " + animationID);
    }

    public void setAnimation(Object animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        animationsByID.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedID != null ? animation(selectedID) : null;
    }

    @Override
    public Object selectedID() {
        return selectedID;
    }

    @Override
    public void selectFrame(Object animationID, int frameIndex) {
        if (!animationID.equals(selectedID)) {
            selectedID = animationID;
            if (currentAnimation() != null) {
                currentAnimation().setFrameIndex(0);
            } else {
                Logger.warn("No animation with ID {} exists", animationID);
            }
        }
    }

    @Override
    public int frameIndex() {
        return currentAnimation() != null ? currentAnimation().frameIndex() : -1;
    }

    @Override
    public void play() {
        if (currentAnimation() != null) {
            currentAnimation().play();
        }
    }

    @Override
    public void stop() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void reset() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}