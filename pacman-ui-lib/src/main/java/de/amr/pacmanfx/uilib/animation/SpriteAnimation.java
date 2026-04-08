/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Plays a sequence of sprite sheet regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation {

    public static final int FPS = 60;
    private static final long ONE_TICK_DURATION_NANOS = 1_000_000_000 / FPS;

    public static SpriteAnimationBuilder builder(SpriteAnimationTimer timer) {
        return new SpriteAnimationBuilder(timer);
    }

    private RectShort[] sprites = new RectShort[0];
    private int currentFrame;

    public SpriteAnimation() {}

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
        frameDuration = ONE_TICK_DURATION_NANOS * numTicks;
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
        currentFrame++;
        if (currentFrame == sprites.length) {
            currentFrame = loop ? 0 : sprites.length - 1;
        }
    }

    private boolean isValidFrame(int index) { return 0 <= index && index < sprites.length; }

    // new

    private boolean loop = false;
    private boolean started = true;
    private long lastUpdateTime;

    private long frameDuration = ONE_TICK_DURATION_NANOS;

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void start() {
        started = true;
    }

    public void stop() {
        started = false;
    }

    public void update(long now) {
        if (!started) {
            return;
        }
        if (now - lastUpdateTime > frameDuration) {
            advanceFrame();
            lastUpdateTime = now;
        }
    }
}