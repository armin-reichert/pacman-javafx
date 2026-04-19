/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

public interface AnimationSet {

    static AnimationSet emptyAnimSet() {
        return EMPTY;
    }

    static AnimationSet singleSpriteAnimSet(RectShort sprite) {
        return new SingleSpriteAnimationSet(sprite);
    }

    AnimationSet EMPTY = new EmptyAnimationSet();

    Object animation(Object animationID);

    Object selectedAnimationID();

    default boolean isSelected(Object animationID) {
        requireNonNull(animationID);
        return animationID.equals(selectedAnimationID());
    }

    void setAnimationFrame(Object animationID, int frameIndex);

    default void selectAnimation(Object animationID) { setAnimationFrame(animationID, 0); }

    void playSelectedAnimation();

    void stopSelectedAnimation();

    void resetSelectedAnimation();

    RectShort currentSprite();

    int currentFrame();
}
