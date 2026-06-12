/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.Identifier;
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
    public Object animation(Identifier animationID) {
        return null;
    }

    @Override
    public Identifier selectedAnimationID() {
        return null;
    }

    @Override
    public boolean isSelected(Identifier animationID) {
        return SpriteAnimationAccessor.super.isSelected(animationID);
    }

    @Override
    public void setAnimationFrame(Identifier animationID, int frameIndex) {}

    @Override
    public void select(Identifier animationID) {}

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
