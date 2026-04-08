/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.uilib.animation.sprite.SpriteAnimationTimer;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationBuilder {

    //TODO
    private static final SpriteAnimationTimer TIMER = new SpriteAnimationTimer();
    {
        TIMER.start();
    }

    private static class BuildData {
        int frameTicks = 1;
        RectShort[] sprites = new RectShort[0];
        boolean stopped = false;
        boolean loop = false;
    }

    private final BuildData data = new BuildData();

    public SpriteAnimationBuilder frameTicks(int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Number of ticks per frame is negative (%d)".formatted(data.frameTicks));
        }
        data.frameTicks = ticks;
        return this;
    }

    public SpriteAnimationBuilder sprites(RectShort[] sprites) {
        data.sprites = requireNonNull(sprites);
        if (Arrays.stream(sprites).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null sprite in sprite array");
        }
        return this;
    }

    public SpriteAnimationBuilder singleSprite(RectShort sprite) {
        return sprites(new RectShort[] { requireNonNull(sprite) });
    }

    public SpriteAnimationBuilder repeated() {
        data.loop = true;
        return this;
    }

    public SpriteAnimationBuilder stopped() {
        data.stopped = true;
        return this;
    }

    public SpriteAnimation build() {
        if (data.sprites == null) {
            throw new IllegalArgumentException("No sprites defined");
        }
        final SpriteAnimation anim = new SpriteAnimation();
        anim.setLoop(data.loop);
        anim.setSprites(data.sprites);
        anim.setFrameTicks(data.frameTicks);
        if (data.stopped) {
            anim.stop();
        }

        //TODO
        TIMER.registerAnimation(anim);

        return anim;
    }
}
