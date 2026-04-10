/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.math.RectShort;

import static java.util.Objects.requireNonNull;

public interface AnimationManager {

    AnimationManager NO_ANIMATIONS = new AnimationManager() {
        @Override
        public Object animation(Object animationID) {
            return null;
        }

        @Override
        public String selectedAnimationID() {
            return "";
        }

        @Override
        public void setAnimationFrame(Object animationID, int frameIndex) {
        }

        @Override
        public void playSelectedAnimation() {
        }

        @Override
        public void stopSelectedAnimation() {
        }

        @Override
        public void resetSelectedAnimation() {
        }

        @Override
        public RectShort currentSprite() {
            return null;
        }

        @Override
        public int frameIndex() {
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

    default void playAnimation(Object animationID) { selectAnimation(animationID); playSelectedAnimation(); }

    void playSelectedAnimation();

    void stopSelectedAnimation();

    void resetSelectedAnimation();

    RectShort currentSprite();

    int frameIndex();
}
