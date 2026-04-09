/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.RectShort;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationBuilder {

    private static class BuildData {
        RectShort[] sprites = new RectShort[0];
        boolean initiallyStopped = false;
        boolean loop = false;
        int frameTicks = 1;
    }

    private final SpriteAnimationManager manager;
    private final BuildData data = new BuildData();

    public SpriteAnimationBuilder(SpriteAnimationManager manager) {
        this.manager = requireNonNull(manager);
    }

    public SpriteAnimationBuilder sprites(RectShort[] sprites) {
        data.sprites = requireNonNull(sprites);
        if (Arrays.stream(sprites).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null entry in sprite array");
        }
        return this;
    }

    public SpriteAnimationBuilder sprite(RectShort sprite) {
        return sprites(new RectShort[] { sprite });
    }

    public SpriteAnimationBuilder frameTicks(int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Number of ticks per frame is negative (%d)".formatted(ticks));
        }
        data.frameTicks = ticks;
        return this;
    }

    public SpriteAnimationBuilder repeated() {
        data.loop = true;
        return this;
    }

    public SpriteAnimationBuilder initiallyStopped() {
        data.initiallyStopped = true;
        return this;
    }

    public SpriteAnimation build() {
        if (data.sprites == null) {
            throw new IllegalArgumentException("No sprites defined");
        }
        final SpriteAnimation anim = new SpriteAnimation(60);
        anim.setLoop(data.loop);
        anim.setSprites(data.sprites);
        anim.setFrameTicks(data.frameTicks);
        if (data.initiallyStopped) {
            anim.stop();
        }
        manager.registerAnimation(anim);
        return anim;
    }
}
