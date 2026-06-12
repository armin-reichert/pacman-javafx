/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.spriteanim;

import de.amr.basics.Identifier;
import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of the sprite animation facade for a single sprite (a big nothingburger).
 *
 * @param sprite the singleton sprite
 */
record SingleSpriteAnimationAccessor(RectShort sprite) implements SpriteAnimationAccessor {

    public SingleSpriteAnimationAccessor(RectShort sprite) {
        this.sprite = requireNonNull(sprite);
    }

    @Override
    public SpriteAnimationContainer container() {
        return null; //TODO check
    }

    @Override
    public RectShort currentSprite() {
        return sprite;
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
    public void setAnimationFrame(Identifier animationID, int frameIndex) {}

    @Override
    public void select(Identifier animationID) {}

    @Override
    public int currentFrame() {
        return 0;
    }

    @Override
    public void playSelected() {}

    @Override
    public void stopSelected() {}

    @Override
    public void resetSelected() {}
}