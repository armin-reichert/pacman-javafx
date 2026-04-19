/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.spriteanim;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.AnimationSet;

import static java.util.Objects.requireNonNull;

public record SingleSpriteNoAnimation(RectShort sprite) implements AnimationSet {

    public SingleSpriteNoAnimation(RectShort sprite) {
        this.sprite = requireNonNull(sprite);
    }

    @Override
    public RectShort currentSprite() {
        return sprite;
    }

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
    public int currentFrame() {
        return 0;
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
}