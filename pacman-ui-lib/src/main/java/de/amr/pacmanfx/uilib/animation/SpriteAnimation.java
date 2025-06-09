/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectArea;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.util.Duration;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Plays a sequence of sprite sheet regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation extends Transition {

    public static class Builder {

        private final SpriteAnimation anim;

        private Builder() {
            anim = new SpriteAnimation();
        }

        public Builder fps(int fps) {
            anim.fps = fps;
            return this;
        }

        public Builder frameTicks(int ticks) {
            anim.frameTicks = ticks;
            return this;
        }

        public Builder sprites(RectArea... sprites) {
            anim.sprites = sprites;
            return this;
        }

        public SpriteAnimation endless() {
            return build(Animation.INDEFINITE);
        }

        public SpriteAnimation end() {
            return build(anim.sprites.length);
        }

        private SpriteAnimation build(int cycleCount) {
            anim.setCycleDuration(Duration.seconds(1.0 / anim.fps * anim.frameTicks));
            anim.setCycleCount(cycleCount);
            anim.setInterpolator(Interpolator.LINEAR);
            Logger.debug("New sprite animation '{}'", anim);
            return anim;
        }
    }

    public static Builder createSpriteAnimation() {
        return new Builder();
    }

    private RectArea[] sprites = new RectArea[0];
    private int fps = 60;
    private int frameTicks = 1;
    private int frameIndex;

    private boolean isValidFrameIndex(int index) { return 0 <= index && index < sprites.length; }

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

    public void setSprites(RectArea[] sprites) {
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

    public RectArea currentSprite() { return sprites[frameIndex]; }

    public void nextFrame() {
        frameIndex++;
        if (frameIndex == sprites.length) {
            frameIndex = getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
        }
    }
}