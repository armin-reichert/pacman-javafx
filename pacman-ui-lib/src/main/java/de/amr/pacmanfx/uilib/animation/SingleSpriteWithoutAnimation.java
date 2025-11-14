/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.AnimationManager;

import static java.util.Objects.requireNonNull;

public class SingleSpriteWithoutAnimation implements AnimationManager {

    private final RectShort sprite;

    public SingleSpriteWithoutAnimation(RectShort sprite) {
        this.sprite = requireNonNull(sprite);
    }

    @Override
    public RectShort currentSprite(Actor actor) { return sprite; }

    @Override
    public Object animation(String id) {
        return null;
    }

    @Override
    public String selectedID() {
        return null;
    }

    @Override
    public void selectFrame(String id, int frameIndex) {}

    @Override
    public int frameIndex() {
        return 0;
    }

    @Override
    public void play() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}
}