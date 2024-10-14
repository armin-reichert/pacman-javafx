/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
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

    private static final int FPS = 60;

    public static class Builder {

        private final SpriteAnimation workPiece;

        private Builder(GameSpriteSheet spriteSheet) {
            workPiece = new SpriteAnimation(spriteSheet);
        }

        public Builder info(String info) {
            workPiece.info = info;
            return this;
        }

        public Builder frameTicks(int ticks) {
            workPiece.frameTicks = ticks;
            return this;
        }

        public Builder sprites(RectArea... sprites) {
            workPiece.sprites = sprites;
            return this;
        }

        public SpriteAnimation loop() {
            workPiece.loop = true;
            workPiece.animator = workPiece.new Animator();
            Logger.debug("New sprite animation '{}'", workPiece.info);
            return workPiece;
        }

        public SpriteAnimation end() {
            Logger.debug("New sprite animation '{}'", workPiece.info);
            workPiece.animator = workPiece.new Animator();
            return workPiece;
        }
    }

    public static Builder spriteSheet(GameSpriteSheet spriteSheet) {
        return new Builder(spriteSheet);
    }

    private class Animator extends Transition {

        private Animator() {
            setCycleDuration(Duration.seconds(1.0 / FPS * frameTicks));
            setCycleCount(loop ? Animation.INDEFINITE : sprites.length);
            setInterpolator(Interpolator.LINEAR);
        }

        @Override
        protected void interpolate(double frac) {
            if (frac >= 1) {
                nextFrame();
            }
        }
    }

    private final GameSpriteSheet spriteSheet;
    private Animator animator;
    private String info;
    private RectArea[] sprites;
    private boolean loop;
    private int frameTicks = 1;
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
        animator.stop();
        animator.jumpTo(Duration.ZERO);
        frameIndex = 0;
    }

    public void setFrameTicks(int ticks) {
        if (ticks != frameTicks) {
            boolean doRestart = animator.getStatus() == Status.RUNNING;
            animator.stop();
            frameTicks = ticks;
            if (doRestart) {
                start();
            }
        }
    }

    public void start() {
        animator.play();
    }

    public void stop() {
        animator.stop();
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
            frameIndex = animator.getCycleCount() == Animation.INDEFINITE ? 0 : sprites.length - 1;
        }
    }
}