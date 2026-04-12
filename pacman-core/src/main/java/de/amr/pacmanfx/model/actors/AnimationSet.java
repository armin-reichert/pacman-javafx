/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.math.RectShort;

import static java.util.Objects.requireNonNull;

public interface AnimationSet {

    AnimationSet EMPTY_ANIMATION_SET = new AnimationSet() {
        @Override
        public Object animation(Object animationID) {
            return null;
        }

        @Override
        public String selectedAnimationID() {
            return null;
        }

        @Override
        public void setAnimationFrame(Object animationID, int frameIndex) {
        }

        @Override
        public void playSelectedAnimation() {}

        @Override
        public void stopSelectedAnimation() {}

        @Override
        public void resetSelectedAnimation() {}

        @Override
        public RectShort currentSprite() {
            return RectShort.ZERO;
        }

        @Override
        public int currentFrame() {
            return -1;
        }
    };

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
