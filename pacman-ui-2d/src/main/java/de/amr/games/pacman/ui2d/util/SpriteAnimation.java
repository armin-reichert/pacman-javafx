/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.rendering.SpriteSheet;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

    public static class Builder {

        private final SpriteAnimation animation = new SpriteAnimation();

        public Builder spriteSheet(SpriteSheet spriteSheet) {
            animation.spriteSheet = spriteSheet;
            return this;
        }

        public Builder frameTicks(int ticks) {
            animation.frameTicks = ticks;
            return this;
        }

        public Builder fps(int fps) {
            animation.fps = fps;
            return this;
        }

        public Builder loop() {
            animation.loop = true;
            return this;
        }

        public Builder sprites(RectangularArea... sprites) {
            animation.sprites = sprites;
            return this;
        }

        public SpriteAnimation end() {
            animation.transition = createTransition(animation);
            return animation;
        }
    }

    public static Builder begin() {
        return new Builder();
    }

    private static Transition createTransition(SpriteAnimation sa) {
        return new Transition() {
            {
                setCycleDuration(Duration.seconds(1.0 / sa.fps * sa.frameTicks));
                setCycleCount(sa.loop ? Animation.INDEFINITE : sa.sprites.length);
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            protected void interpolate(double frac) {
                if (frac == 1.0) {
                    sa.nextFrame();
                }
            }
        };
    }

    private SpriteSheet spriteSheet;
    private RectangularArea[] sprites = new RectangularArea[0];
    private boolean loop;
    private int frameTicks = 1;
    private int fps = 60;
    private Transition transition;
    private int frameIndex;

    public void setSprites(RectangularArea[] sprites) {
        this.sprites = sprites;
        // TODO what about frame index?
    }

    public SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    public RectangularArea[] getSprites() {
        return sprites;
    }

    public void reset() {
        transition.stop();
        transition.jumpTo(Duration.ZERO);
        frameIndex = 0;
    }

    public void setFrameTicks(int ticks) {
        if (ticks != frameTicks) {
            boolean wasRunning = transition.getStatus() == Status.RUNNING;
            transition.stop();
            frameTicks = ticks;
            transition = createTransition(this);
            if (wasRunning) {
                start();
            }
        }
    }

    public void start() {
        transition.play();
    }

    public void stop() {
        transition.stop();
    }

    public boolean isRunning() {
        return transition.getStatus() == Status.RUNNING;
    }

    public void setDelay(Duration delay) {
        transition.setDelay(delay);
    }

    public void setFrameIndex(int index) {
        if (index < 0 || index >= sprites.length) {
            Logger.error("Frame index {} is out of range, Number of sprites: {}",
                index, sprites.length);
        } else {
            frameIndex = index;
        }
    }

    public int frameIndex() {
        return frameIndex;
    }

    public RectangularArea currentSprite() {
        if (frameIndex < sprites.length) {
            return sprites[frameIndex];
        }
        Logger.warn("No sprite for frame index {}", frameIndex);
        return RectangularArea.PIXEL;
    }

    public void nextFrame() {
        frameIndex++;
        if (frameIndex == sprites.length) {
            frameIndex = transition.getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
        }
    }
}