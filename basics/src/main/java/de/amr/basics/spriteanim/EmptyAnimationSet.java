/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

public class EmptyAnimationSet implements AnimationSet {
    @Override
    public Object animation(Object animationID) {
        return null;
    }

    @Override
    public String selectedAnimationID() {
        return null;
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {}

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
