/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import static java.util.Objects.requireNonNull;

/**
 * Plays a sequence of image regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation {

    private final SpriteAnimationContainer container;
    private final int fps;
    private RectShort[] sprites;
    private int currentFrameIndex;
    private boolean loop;
    private boolean running;
    private long lastUpdateTime;
    private long frameDuration;

    public SpriteAnimation(SpriteAnimationContainer container, int fps) {
        this.container = requireNonNull(container);
        if (fps <= 0) {
            throw new IllegalArgumentException("Illegal FPS value: %d".formatted(fps));
        }
        this.fps = fps;
        sprites = new RectShort[0];
        currentFrameIndex = 0;
        loop = false;
        running = false;
        lastUpdateTime = now();
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
        if (!running) {
            container.register(this);
            running = true;
            lastUpdateTime = now();
        }
    }

    public void stop() {
        container.unregister(this);
        running = false;
    }

    public void reset() {
        stop();
        currentFrameIndex = 0;
        lastUpdateTime = now();
    }

    public void setSprites(RectShort[] sprites) {
        if (this.sprites == sprites) {
            //Logger.info("Sprites unchanged");
            return;
        }
        this.sprites = requireNonNull(sprites);
        if (sprites.length == 0) {
            throw new IllegalArgumentException("Sprites array is empty");
        }
    }

    public void setFrameTicks(int numTicks) {
        if (numTicks <= 0) {
            throw new IllegalArgumentException("Frame ticks must be a positive number, but you gave me " + numTicks);
        }
        frameDuration = 1_000_000_000L / fps * numTicks;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setCurrentFrameIndex(int frame) {
        if (!isValidFrame(frame)) {
            throw new IllegalArgumentException("Frame %d is out of range, number of sprites: %d".formatted(frame, sprites.length));
        }
        currentFrameIndex = frame;
    }

    public int currentFrame() { return currentFrameIndex; }

    public RectShort currentSprite() { return sprites[currentFrameIndex]; }

    public void advanceFrame() {
        if (currentFrameIndex == sprites.length - 1) {
            if (loop) {
                currentFrameIndex = 0;
            } else {
                stop();
            }
        } else {
            ++currentFrameIndex;
        }
    }

    private boolean isValidFrame(int index) { return 0 <= index && index < sprites.length; }

    private long now() {
        return System.nanoTime();
    }
}