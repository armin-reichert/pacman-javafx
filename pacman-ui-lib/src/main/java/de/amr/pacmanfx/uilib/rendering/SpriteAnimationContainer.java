/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.AnimationFacade;
import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A sprite animation container implementing the sprite animation facade interface.
 */
public abstract class SpriteAnimationContainer<SID> implements AnimationFacade {

    protected final SpriteSheet<SID> spriteSheet;
    protected final Map<AnimationIdentifier, SpriteAnimation> animationsByID = new HashMap<>();
    protected AnimationIdentifier selectedAnimationID;

    public SpriteAnimationContainer(SpriteSheet<SID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    protected abstract SpriteAnimation createAnimation(AnimationIdentifier animationID);

    public SpriteSheet<SID> spriteSheet() { return spriteSheet; }

    public boolean isSelected(AnimationIdentifier id) {
        requireNonNull(id);
        return id.equals(selectedAnimationID);
    }

    @Override
    public void select(AnimationIdentifier animationID) {
        selectedAnimationID = animationID;
    }

    @Override
    public RectShort currentSprite() {
        final SpriteAnimation currentAnimation = currentAnimation();
        return currentAnimation == null ? null : currentAnimation.currentSprite();
    }

    @Override
    public SpriteAnimation animation(AnimationIdentifier animationID) {
        if (!animationsByID.containsKey(animationID)) {
            SpriteAnimation spriteAnimation = createAnimation(animationID);
            animationsByID.put(animationID, spriteAnimation);
        }
        return animationsByID.get(animationID);
    }

    public void setAnimation(AnimationIdentifier animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        animationsByID.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedAnimationID != null ? animation(selectedAnimationID) : null;
    }

    @Override
    public AnimationIdentifier selectedAnimationID() {
        return selectedAnimationID;
    }

    @Override
    public void setAnimationFrame(AnimationIdentifier animationID, int frameIndex) {
        if (!animationID.equals(selectedAnimationID)) {
            selectedAnimationID = animationID;
            if (currentAnimation() != null) {
                currentAnimation().setCurrentFrameIndex(0);
            } else {
                Logger.warn("No animation with ID {} exists", animationID);
            }
        }
    }

    @Override
    public int currentFrame() {
        return currentAnimation() != null ? currentAnimation().currentFrame() : -1;
    }

    @Override
    public void playSelected() {
        if (currentAnimation() != null) {
            currentAnimation().start();
        }
    }

    @Override
    public void stopSelected() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void resetSelected() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}