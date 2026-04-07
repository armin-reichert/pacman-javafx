/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.RectShort;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Plays a sequence of sprite sheet regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation extends Transition {

    public static final int FPS = 60;

    public static SpriteAnimationBuilder builder() {
        return new SpriteAnimationBuilder();
    }

    private RectShort[] sprites = new RectShort[0];
    private int frameTicks = 1;
    private int frameIndex;

    private boolean isValidFrameIndex(int index) { return 0 <= index && index < sprites.length; }

    public SpriteAnimation(Duration cycleDuration) {
        requireNonNull(cycleDuration);
        setCycleDuration(cycleDuration);
    }

    @Override
    protected void interpolate(double t) {
        if (t == 1) {
            nextFrame();
        }
    }

    public void reset() {
        stop();
        jumpTo(Duration.ZERO);
        frameIndex = 0;
    }

    public void setSprites(RectShort[] sprites) {
        this.sprites = requireNonNull(sprites);
    }

    public void setFrameTicks(int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Frame ticks must be a positive number, but you gave me " + ticks);
        }
        if (ticks != frameTicks) {
            boolean doRestart = getStatus() == Status.RUNNING;
            stop();
            frameTicks = ticks;
            if (doRestart) {
                play();
            }
        }
    }

    public void setFrameIndex(int index) {
        if (isValidFrameIndex(index)) {
            frameIndex = index;
        } else {
            throw new IllegalArgumentException("Frame index %d is out of range, number of sprites: %d".formatted(index, sprites.length));
        }
    }

    public int frameIndex() { return frameIndex; }

    public RectShort currentSprite() { return sprites[frameIndex]; }

    public void nextFrame() {
        frameIndex++;
        if (frameIndex == sprites.length) {
            frameIndex = getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
        }
    }
}