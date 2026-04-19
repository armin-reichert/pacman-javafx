/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

public interface SpriteAnimationSet {

    static SpriteAnimationSet emptyAnimSet() {
        return EMPTY;
    }

    static SpriteAnimationSet singleSpriteAnimSet(RectShort sprite) {
        return new SingleSpriteSpriteAnimationSet(sprite);
    }

    SpriteAnimationSet EMPTY = new EmptySpriteAnimationSet();

    Object animation(SpriteAnimationID animationID);

    SpriteAnimationID selectedAnimationID();

    default boolean isSelected(SpriteAnimationID animationID) {
        requireNonNull(animationID);
        return animationID.equals(selectedAnimationID());
    }

    void setAnimationFrame(SpriteAnimationID animationID, int frameIndex);

    default void selectAnimation(SpriteAnimationID animationID) { setAnimationFrame(animationID, 0); }

    void playSelectedAnimation();

    void stopSelectedAnimation();

    void resetSelectedAnimation();

    RectShort currentSprite();

    int currentFrame();
}
