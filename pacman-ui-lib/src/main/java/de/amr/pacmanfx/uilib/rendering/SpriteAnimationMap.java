/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationFacade;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A sprite animation container implementing the sprite animation facade interface.
 *
 * @param <ID> type of sprite animation identifiers
 */
public abstract class SpriteAnimationMap<ID extends AnimationIdentifier> implements SpriteAnimationFacade {

    protected final SpriteSheet<ID> spriteSheet;
    protected final Map<AnimationIdentifier, SpriteAnimation> animationsByID = new HashMap<>();
    protected ID selectedID;

    public SpriteAnimationMap(SpriteSheet<ID> spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    protected abstract SpriteAnimation createAnimation(AnimationIdentifier animationID);

    public SpriteSheet<ID> spriteSheet() { return spriteSheet; }

    public boolean isSelected(AnimationIdentifier id) {
        requireNonNull(id);
        return id.equals(selectedID);
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

    public void setAnimation(ID animationID, SpriteAnimation animation) {
        requireNonNull(animationID);
        requireNonNull(animation);
        animationsByID.put(animationID, animation);
    }

    public SpriteAnimation currentAnimation() {
        return selectedID != null ? animation(selectedID) : null;
    }

    @Override
    public ID selectedAnimationID() {
        return selectedID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAnimationFrame(AnimationIdentifier animationID, int frameIndex) {
        if (!animationID.equals(selectedID)) {
            selectedID = (ID) animationID;
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
    public void playSelectedAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().start();
        }
    }

    @Override
    public void stopSelectedAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().stop();
        }
    }

    @Override
    public void resetSelectedAnimation() {
        if (currentAnimation() != null) {
            currentAnimation().reset();
        }
    }
}