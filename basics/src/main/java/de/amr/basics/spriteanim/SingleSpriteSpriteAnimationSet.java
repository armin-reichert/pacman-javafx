/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

record SingleSpriteSpriteAnimationSet(RectShort sprite) implements SpriteAnimationSet {

    public SingleSpriteSpriteAnimationSet(RectShort sprite) {
        this.sprite = requireNonNull(sprite);
    }

    @Override
    public RectShort currentSprite() {
        return sprite;
    }

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
    public int currentFrame() {
        return 0;
    }

    @Override
    public void playSelectedAnimation() {}

    @Override
    public void stopSelectedAnimation() {}

    @Override
    public void resetSelectedAnimation() {}
}