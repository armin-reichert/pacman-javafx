/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Facade for accessing sprite animations.
 */
public interface SpriteAnimationFacade {

    SpriteAnimationFacade EMPTY_FACADE = new EmptySpriteAnimationFacade();

    static SpriteAnimationFacade emptyAnimationFacade() {
        return EMPTY_FACADE;
    }

    static SpriteAnimationFacade singleSpriteAnimationFacade(RectShort sprite) {
        return new SingletonSpriteAnimationFacade(sprite);
    }

    Object animation(AnimationIdentifier animationID);

    AnimationIdentifier selectedAnimationID();

    default boolean isSelected(AnimationIdentifier animationID) {
        requireNonNull(animationID);
        return animationID.equals(selectedAnimationID());
    }

    void setAnimationFrame(AnimationIdentifier animationID, int frameIndex);

    default void selectAnimation(AnimationIdentifier animationID) { setAnimationFrame(animationID, 0); }

    void playSelectedAnimation();

    void stopSelectedAnimation();

    void resetSelectedAnimation();

    RectShort currentSprite();

    int currentFrame();
}
