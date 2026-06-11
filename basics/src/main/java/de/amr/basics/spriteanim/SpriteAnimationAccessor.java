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
public interface SpriteAnimationAccessor {

    SpriteAnimationAccessor EMPTY_SPRITE_ANIMATION_ACCESSOR = new EmptySpriteAnimationAccessor();

    static SpriteAnimationAccessor emptyAnimation() {
        return EMPTY_SPRITE_ANIMATION_ACCESSOR;
    }

    static SpriteAnimationAccessor singleSpriteAnimation(RectShort sprite) {
        return new SingleSpriteAnimationAccessor(sprite);
    }

    SpriteAnimationContainer container();

    Object animation(Named animationID);

    Named selectedAnimationID();

    default boolean isSelected(Named animationID) {
        requireNonNull(animationID);
        return animationID == selectedAnimationID();
    }

    void setAnimationFrame(Named animationID, int frameIndex);

    void select(Named animationID);

    default void selectAndSetFrame(Named animationID, int frameIndex) {
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
