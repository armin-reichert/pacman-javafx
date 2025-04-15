/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.animation;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.uilib.assets.SpriteSheet;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.util.Duration;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation extends Transition {

    private static final int FPS = 60;

    public static class Builder {

        private final SpriteAnimation workPiece;

        private Builder(SpriteSheet spriteSheet) {
            workPiece = new SpriteAnimation(spriteSheet);
        }

        public Builder frameTicks(int ticks) {
            workPiece.frameTicks = ticks;
            return this;
        }

        public Builder sprites(RectArea... sprites) {
            workPiece.sprites = sprites;
            return this;
        }

        public SpriteAnimation endLoop() {
            return build(Animation.INDEFINITE);
        }

        public SpriteAnimation end() {
            return build(workPiece.sprites.length);
        }

        private SpriteAnimation build(int cycleCount) {
            workPiece.setCycleDuration(Duration.seconds(1.0 / FPS * workPiece.frameTicks));
            workPiece.setCycleCount(cycleCount);
            workPiece.setInterpolator(Interpolator.LINEAR);
            Logger.debug("New sprite animation '{}'", workPiece);
            return workPiece;
        }
    }

    public static Builder spriteSheet(SpriteSheet spriteSheet) {
        return new Builder(spriteSheet);
    }

    private final SpriteSheet spriteSheet;
    private RectArea[] sprites = new RectArea[0];
    private int frameTicks = 1;
    private int frameIndex;

    private SpriteAnimation(SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
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

    public SpriteSheet spriteSheet() {
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