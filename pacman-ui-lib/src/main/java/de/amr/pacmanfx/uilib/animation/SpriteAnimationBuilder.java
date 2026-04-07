/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.RectShort;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SpriteAnimationBuilder {

    private static class BuildData {
        int frameTicks = 1;
        RectShort[] sprites = new RectShort[0];
    }

    private final BuildData data = new BuildData();

    public SpriteAnimationBuilder ticksPerFrame(int ticks) {
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
        data.sprites = new RectShort[]{requireNonNull(sprite)};
        return this;
    }

    public SpriteAnimation repeated() {
        return build(Animation.INDEFINITE);
    }

    public SpriteAnimation once() {
        return build(data.sprites.length);
    }

    private SpriteAnimation build(int cycleCount) {
        if (data.sprites == null) {
            throw new IllegalArgumentException("No sprites defined");
        }
        if (data.frameTicks <= 0) {
            throw new IllegalArgumentException("Number of ticks per frame is negative (%d)".formatted(data.frameTicks));
        }
        final Duration cycleDuration = Duration.seconds(1.0 / SpriteAnimation.FPS * data.frameTicks);
        final SpriteAnimation anim = new SpriteAnimation(cycleDuration);
        anim.setCycleCount(cycleCount);
        anim.setInterpolator(Interpolator.LINEAR);
        anim.setSprites(data.sprites);
        return anim;
    }
}
