/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;

/**
 * Null object for sprite animation facade interface.
 */
public class EmptySpriteAnimationAccessor implements SpriteAnimationAccessor {

    @Override
    public SpriteAnimationContainer container() {
        return null;
    }

    @Override
    public Object animation(Named animationID) {
        return null;
    }

    @Override
    public Named selectedAnimationID() {
        return null;
    }

    @Override
    public boolean isSelected(Named animationID) {
        return SpriteAnimationAccessor.super.isSelected(animationID);
    }

    @Override
    public void setAnimationFrame(Named animationID, int frameIndex) {}

    @Override
    public void select(Named animationID) {}

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
