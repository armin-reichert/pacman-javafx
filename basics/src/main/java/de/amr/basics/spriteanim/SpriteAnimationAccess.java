/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Identifier;
import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Facade for accessing animations.
 */
public interface SpriteAnimationAccess {

    SpriteAnimationAccess EMPTY_SPRITE_ANIMATION_ACCESSOR = new EmptySpriteAnimationAccess();

    static SpriteAnimationAccess emptyAnimation() {
        return EMPTY_SPRITE_ANIMATION_ACCESSOR;
    }

    static SpriteAnimationAccess singleSpriteAnimation(RectShort sprite) {
        return new SingleSpriteAnimationAccess(sprite);
    }

    default boolean isEmpty() {
        return this == EMPTY_SPRITE_ANIMATION_ACCESSOR;
    }

    Object animation(Identifier animationID);

    Identifier selectedAnimationID();

    default boolean isSelected(Identifier animationID) {
        requireNonNull(animationID);
        return animationID == selectedAnimationID();
    }

    void setAnimationFrame(Identifier animationID, int frameIndex);

    void select(Identifier animationID);

    default void selectAndSetFrame(Identifier animationID, int frameIndex) {
        select(animationID);
        setAnimationFrame(animationID, frameIndex);
    }

    RectShort currentSprite();

    int currentFrame();

    // playing

    void playSelected();

    void stopSelected();

    void resetSelected();
}
