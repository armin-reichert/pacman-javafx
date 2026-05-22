/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

/**
 * Null object for sprite animation facade interface.
 */
public class EmptySpriteAnimationFacade implements SpriteAnimationFacade {

    @Override
    public Object animation(AnimationIdentifier animationID) {
        return null;
    }

    @Override
    public AnimationIdentifier selectedAnimationID() {
        return null;
    }

    @Override
    public void setAnimationFrame(AnimationIdentifier animationID, int frameIndex) {}

    @Override
    public void playSelectedAnimation() {}

    @Override
    public void stopSelectedAnimation() {}

    @Override
    public void resetSelectedAnimation() {}

    @Override
    public RectShort currentSprite() {
        return RectShort.NULL_RECTANGLE;
    }

    @Override
    public int currentFrame() {
        return -1;
    }
}
