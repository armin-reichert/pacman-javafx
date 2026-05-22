/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Facade for accessing animations.
 */
public interface AnimationFacade {

    AnimationFacade EMPTY_FACADE = new EmptyAnimationFacade();

    static AnimationFacade emptyAnimationFacade() {
        return EMPTY_FACADE;
    }

    static AnimationFacade singletonAnimationFacade(RectShort sprite) {
        return new SingletonAnimationFacade(sprite);
    }

    Object animation(AnimationIdentifier animationID);

    AnimationIdentifier selectedAnimationID();

    default boolean isSelected(AnimationIdentifier animationID) {
        requireNonNull(animationID);
        return animationID == selectedAnimationID();
    }

    void setAnimationFrame(AnimationIdentifier animationID, int frameIndex);

    void select(AnimationIdentifier animationID);

    default void selectAtFrame(AnimationIdentifier animationID, int frameIndex) {
        select(animationID);
        setAnimationFrame(animationID, frameIndex);
    }

    void playSelected();

    void stopSelected();

    void resetSelected();

    RectShort currentSprite();

    int currentFrame();
}
