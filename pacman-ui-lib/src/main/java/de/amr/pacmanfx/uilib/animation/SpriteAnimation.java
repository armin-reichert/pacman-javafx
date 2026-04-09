/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Plays a sequence of sprite sheet regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation {

    public static SpriteAnimationBuilder builder(SpriteAnimationManager timer) {
        return new SpriteAnimationBuilder(timer);
    }

    private final int fps;
    private RectShort[] sprites;
    private int currentFrame;
    private boolean loop;
    private boolean running;
    private long lastUpdateTime;
    private long frameDuration;

    public SpriteAnimation(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("Illegal FPS value: %d".formatted(fps));
        }
        this.fps = fps;
        sprites = new RectShort[0];
        currentFrame = 0;
        loop = false;
        running = true; //TODO check this
        lastUpdateTime = 0;
        setFrameTicks(1);
    }

    public void update(long now) {
        if (!running) {
            return;
        }
        if (now - lastUpdateTime > frameDuration) {
            advanceFrame();
            lastUpdateTime = now;
        }
    }
    public void start() {
        running = true;
        lastUpdateTime = 0;
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        stop();
        currentFrame = 0;
    }

    public void setSprites(RectShort[] sprites) {
        this.sprites = requireNonNull(sprites);
    }

    public void setFrameTicks(int numTicks) {
        if (numTicks <= 0) {
            throw new IllegalArgumentException("Frame ticks must be a positive number, but you gave me " + numTicks);
        }
        frameDuration = 1_000_000_000L / fps * numTicks;
    }

    public void setCurrentFrame(int frame) {
        if (!isValidFrame(frame)) {
            throw new IllegalArgumentException("Frame %d is out of range, number of sprites: %d".formatted(frame, sprites.length));
        }
        currentFrame = frame;
    }

    public int currentFrame() { return currentFrame; }

    public RectShort currentSprite() { return sprites[currentFrame]; }

    public void advanceFrame() {
        if (currentFrame == sprites.length - 1) {
            if (loop) {
                currentFrame = 0;
            } else {
                stop();
            }
        } else {
            ++currentFrame;
        }
    }

    private boolean isValidFrame(int index) { return 0 <= index && index < sprites.length; }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

}