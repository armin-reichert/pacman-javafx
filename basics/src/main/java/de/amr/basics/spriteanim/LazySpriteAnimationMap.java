/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A sprite animation container implementing the sprite animation accessor facade.
 */
public class LazySpriteAnimationMap implements SpriteAnimationAccessor {

    private Map<Named, SpriteAnimation> animationsByID;
    protected Named selectedAnimationID;
    protected Function<Named, SpriteAnimation> factory;

    protected LazySpriteAnimationMap() {}

    private void createMapIfNotExisting() {
        if (animationsByID == null) {
            animationsByID = new HashMap<>();
        }
    }

    public void setFactory(Function<Named, SpriteAnimation> factory) {
        this.factory = factory;
    }

    public boolean isSelected(Named id) {
        requireNonNull(id);
        return id.equals(selectedAnimationID);
    }

    @Override
    public void select(Named animationID) {
        selectedAnimationID = animationID;
    }

    @Override
    public RectShort currentSprite() {
        final SpriteAnimation anim = currentAnimation();
        return anim == null ? null : anim.sprite();
    }

    @Override
    public SpriteAnimation animation(Named animationID) {
        createMapIfNotExisting();
        if (!animationsByID.containsKey(animationID)) {
            final SpriteAnimation anim = factory.apply(animationID);
            animationsByID.put(animationID, anim);
        }
        return animationsByID.get(animationID);
    }

    public void setAnimation(Named animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        createMapIfNotExisting();
        animationsByID.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedAnimationID != null ? animation(selectedAnimationID) : null;
    }

    @Override
    public Named selectedAnimationID() {
        return selectedAnimationID;
    }

    @Override
    public void setAnimationFrame(Named animationID, int frameIndex) {
        if (!animationID.equals(selectedAnimationID)) {
            selectedAnimationID = animationID;
        }
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.setFrame(frameIndex);
        } else {
            Logger.warn("Cannot set animation to frame {}: no animation with ID {} exists", frameIndex, animationID);
        }
    }

    @Override
    public int currentFrame() {
        final SpriteAnimation anim = currentAnimation();
        return anim != null ? anim.frame() : -1;
    }

    @Override
    public int numFrames() {
        final SpriteAnimation anim = currentAnimation();
        return anim != null ? anim.numFrames() : 0;
    }

    @Override
    public void playSelected() {
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.start();
        }
    }

    @Override
    public void stopSelected() {
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.stop();
        }
    }

    @Override
    public void resetSelected() {
        final SpriteAnimation anim = currentAnimation();
        if (anim != null) {
            anim.reset();
        }
    }
}