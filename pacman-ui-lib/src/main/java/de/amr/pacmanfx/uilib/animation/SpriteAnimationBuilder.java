/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.basics.math.RectShort;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationBuilder {

    /**
     * Creates a new sprite animation builder for animations running at 60 frames/second.
     *
     * @return new  builder
     */
    public static SpriteAnimationBuilder builder() {
        return new SpriteAnimationBuilder(60);
    }

    /**
     * Creates a new sprite animation builder for animations running at the specified frame rate (frames/second).
     *
     * @param fps the frame rate at which the build animation is played
     * @return new  builder
     */
    public static SpriteAnimationBuilder builder(int fps) {
        return new SpriteAnimationBuilder(fps);
    }

    private static class BuildData {
        RectShort[] sprites;
        boolean initiallyStopped = false;
        boolean loop = false;
        int fps;
        int frameTicks = 1;
    }

    private BuildData data;

    private void checkBuildPossible() {
        if (data == null) {
            throw new IllegalStateException("Build method can only be called once");
        }
    }

    private SpriteAnimationBuilder(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("Sprite animation frame rate must be positive, but is %d".formatted(fps));
        }
        data = new BuildData();
        data.fps = fps;
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
        final SpriteAnimation animation = new SpriteAnimation(data.fps);
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
