/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Facade for accessing animations.
 */
public interface SpriteAnimationFacade {

    SpriteAnimationFacade EMPTY_SPRITE_ANIMATION_FACADE = new EmptySpriteAnimationFacade();

    static SpriteAnimationFacade singleSpriteAnimationFacade(RectShort sprite) {
        return new SingletonSpriteAnimationFacade(sprite);
    }

    default boolean isEmpty() {
        return this == EMPTY_SPRITE_ANIMATION_FACADE;
    }

    Object animation(Named animationID);

    Named selectedAnimationID();

    default boolean isSelected(Named animationID) {
        requireNonNull(animationID);
        return animationID == selectedAnimationID();
    }

    void setAnimationFrame(Named animationID, int frameIndex);

    void select(Named animationID);

    RectShort currentSprite();

    int currentFrame();

    int numFrames();

    // playing

    void playSelected();

    void stopSelected();

    void resetSelected();
}
