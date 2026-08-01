/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Naming;
import de.amr.basics.math.RectShort;

/**
 * Null object for sprite animation facade interface.
 */
public class EmptySpriteAnimationAccess implements SpriteAnimationAccess {

    @Override
    public Object animation(Naming animationID) {
        return null;
    }

    @Override
    public Naming selectedAnimationID() {
        return null;
    }

    @Override
    public boolean isSelected(Naming animationID) {
        return SpriteAnimationAccess.super.isSelected(animationID);
    }

    @Override
    public void setAnimationFrame(Naming animationID, int frameIndex) {}

    @Override
    public void select(Naming animationID) {}

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

    @Override
    public int numFrames() {
        return 0;
    }
}
