/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

/**
 * Null object for sprite animation facade interface.
 */
public class EmptyAnimationFacade implements AnimationFacade {

    @Override
    public Object animation(AnimationIdentifier animationID) {
        return null;
    }

    @Override
    public AnimationIdentifier selectedAnimationID() {
        return null;
    }

    @Override
    public boolean isSelected(AnimationIdentifier animationID) {
        return AnimationFacade.super.isSelected(animationID);
    }

    @Override
    public void setAnimationFrame(AnimationIdentifier animationID, int frameIndex) {}

    @Override
    public void select(AnimationIdentifier animationID) {}

    @Override
    public void playSelected() {}

    @Override
    public void stopSelected() {}

    @Override
    public void resetSelected() {}

    @Override
    public RectShort currentSprite() {
        return RectShort.NULL_RECTANGLE;
    }

    @Override
    public int currentFrame() {
        return -1;
    }
}
