/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

public class EmptySpriteAnimationSet implements SpriteAnimationSet {
    @Override
    public Object animation(SpriteAnimationID animationID) {
        return null;
    }

    @Override
    public SpriteAnimationID selectedAnimationID() {
        return null;
    }

    @Override
    public void setAnimationFrame(SpriteAnimationID animationID, int frameIndex) {}

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
