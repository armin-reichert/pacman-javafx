/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;

import static java.util.Objects.requireNonNull;

public class SingleSpriteWithoutAnimation implements ActorAnimationMap {

    private final Sprite sprite;

    public SingleSpriteWithoutAnimation(Sprite sprite) {
        this.sprite = requireNonNull(sprite);
    }

    public Sprite singleSprite() { return sprite; }

    @Override
    public Object animation(String id) {
        return null;
    }

    @Override
    public String selectedAnimationID() {
        return null;
    }

    @Override
    public void selectAnimationAtFrame(String id, int frameIndex) {}

    @Override
    public void play() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}