/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationBuilder {

    private static class BuildData {
        RectShort[] sprites;
        boolean initiallyStopped = false;
        boolean loop = false;
        int frameTicks = 1;
    }

    private BuildData data;

    private void checkBuildPossible() {
        if (data == null) {
            throw new IllegalStateException("Build method can only be called once");
        }
    }

    public SpriteAnimationBuilder() {
        data = new BuildData();
    }

    public SpriteAnimationBuilder sprites(RectShort... sprites) {
        checkBuildPossible();
        if (data.sprites != null) {
            throw new IllegalArgumentException("Cannot set sprites: Sprite(s) already defined");
        }
        data.sprites = requireNonNull(sprites);
        if (Arrays.stream(sprites).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null entry in sprite array");
        }
        return this;
    }

    public SpriteAnimationBuilder singleSprite(RectShort sprite) {
        checkBuildPossible();
        if (data.sprites != null) {
            throw new IllegalArgumentException("Cannot set single sprite: Sprite(s) already defined");
        }
        if (sprite == null) {
            throw new IllegalArgumentException("Cannot set single sprite: Sprite is null");
        }
        return sprites(sprite);
    }

    public SpriteAnimationBuilder frameTicks(int ticks) {
        checkBuildPossible();
        if (ticks <= 0) {
            throw new IllegalArgumentException("Number of ticks per frame (%d) must be positive".formatted(ticks));
        }
        data.frameTicks = ticks;
        return this;
    }

    public SpriteAnimationBuilder repeated() {
        checkBuildPossible();
        data.loop = true;
        return this;
    }

    public SpriteAnimationBuilder initiallyStopped() {
        checkBuildPossible();
        data.initiallyStopped = true;
        return this;
    }

    public SpriteAnimation build() {
        checkBuildPossible();
        if (data.sprites == null) {
            throw new IllegalArgumentException("No sprites defined");
        }
        final SpriteAnimation animation = new SpriteAnimation();
        animation.setLoop(data.loop);
        animation.setSprites(data.sprites);
        animation.setFrameTicks(data.frameTicks);
        if (data.initiallyStopped) {
            animation.stop();
        }
        data = null; // signals build has been called and data are consumed
        return animation;
    }
}
