/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of the sprite animation facade for a single sprite (a big nothingburger).
 *
 * @param sprite the singleton sprite
 */
record SingletonSpriteAnimationFacade(RectShort sprite) implements SpriteAnimationFacade {

    public SingletonSpriteAnimationFacade(RectShort sprite) {
        this.sprite = requireNonNull(sprite);
    }

    @Override
    public RectShort currentSprite() {
        return sprite;
    }

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