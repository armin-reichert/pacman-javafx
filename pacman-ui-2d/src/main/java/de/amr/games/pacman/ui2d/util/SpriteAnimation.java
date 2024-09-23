/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.RectArea;
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

        private final SpriteAnimation spriteAnimation;

        private Builder(GameSpriteSheet spriteSheet) {
            spriteAnimation = new SpriteAnimation(spriteSheet);
        }

        public Builder frameTicks(int ticks) {
            spriteAnimation.frameTicks = ticks;
            return this;
        }

        public Builder fps(int fps) {
            spriteAnimation.fps = fps;
            return this;
        }

        public Builder sprites(RectArea... sprites) {
            spriteAnimation.sprites = sprites;
            return this;
        }

        public SpriteAnimation loop() {
            spriteAnimation.loop = true;
            spriteAnimation.animation = createTransition(spriteAnimation);
            return spriteAnimation;
        }

        public SpriteAnimation end() {
            spriteAnimation.animation = createTransition(spriteAnimation);
            return spriteAnimation;
        }
    }

    public static Builder spriteSheet(GameSpriteSheet spriteSheet) {
        return new Builder(spriteSheet);
    }

    private static Transition createTransition(SpriteAnimation spriteAnimation) {
        return new Transition() {
            {
                setCycleDuration(Duration.seconds(1.0 / spriteAnimation.fps * spriteAnimation.frameTicks));
                setCycleCount(spriteAnimation.loop ? Animation.INDEFINITE : spriteAnimation.sprites.length);
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            protected void interpolate(double frac) {
                if (frac >= 1) {
                    spriteAnimation.nextFrame();
                }
            }
        };
    }

    private final GameSpriteSheet spriteSheet;
    private RectArea[] sprites;
    private boolean loop;
    private int frameTicks = 1;
    private int fps = 60;
    private Animation animation;
    private int frameIndex;

    private SpriteAnimation(GameSpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    public void setSprites(RectArea[] sprites) {
        this.sprites = sprites;
    }

    public void reset() {
        animation.stop();
        animation.jumpTo(Duration.ZERO);
        frameIndex = 0;
    }

    public void setFrameTicks(int ticks) {
        if (ticks != frameTicks) {
            boolean wasRunning = animation.getStatus() == Status.RUNNING;
            animation.stop();
            frameTicks = ticks;
            animation = createTransition(this);
            if (wasRunning) {
                start();
            }
        }
    }

    public void start() {
        animation.play();
    }

    public void stop() {
        animation.stop();
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
            frameIndex = animation.getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
        }
    }
}