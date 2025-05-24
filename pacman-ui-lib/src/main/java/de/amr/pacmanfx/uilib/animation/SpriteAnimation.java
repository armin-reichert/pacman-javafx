/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * Plays a sequence of sprite sheet regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation extends Transition {

    public static class Builder {

        private final SpriteAnimation anim;

        private Builder(SpriteSheet spriteSheet) {
            anim = new SpriteAnimation(spriteSheet);
        }

        public Builder fps(int fps) {
            anim.fps = fps;
            return this;
        }

        public Builder frameTicks(int ticks) {
            anim.frameTicks = ticks;
            return this;
        }

        public Builder take(RectArea... sprites) {
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

    public static Builder from(SpriteSheet spriteSheet) {
        return new Builder(spriteSheet);
    }

    private final SpriteSheet spriteSheet;
    private RectArea[] sprites = new RectArea[0];
    private int fps = 60;
    private int frameTicks = 1;
    private int frameIndex;

    private SpriteAnimation(SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
        setCycleDuration(Duration.seconds(1.0 / fps));
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

    public SpriteSheet from() {
        return spriteSheet;
    }

    public void setSprites(RectArea[] sprites) {
        this.sprites = sprites;
    }

    public void setFrameTicks(int ticks) {
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
        if (index < 0 || index >= sprites.length) {
            Logger.error("Frame index {} is out of range, Number of sprites: {}", index, sprites.length);
        } else {
            frameIndex = index;
        }
    }

    public int frameIndex() {
        return frameIndex;
    }

    public RectArea currentSprite() {
        if (frameIndex < sprites.length) {
            return sprites[frameIndex];
        }
        Logger.warn("No sprite for frame index {}", frameIndex);
        return RectArea.PIXEL;
    }

    public void nextFrame() {
        frameIndex++;
        if (frameIndex == sprites.length) {
            frameIndex = getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
        }
    }
}