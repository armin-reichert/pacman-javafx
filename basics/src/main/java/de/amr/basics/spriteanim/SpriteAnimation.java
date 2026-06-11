/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.spriteanim;

import de.amr.basics.math.RectShort;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Plays a sequence of image regions ("sprites") to create an animation effect.
 */
public class SpriteAnimation {

    public static final int FRAME_RATE = 60;

    private SpriteAnimationSet container;

    private RectShort[] sprites;
    private int currentFrameIndex;
    private boolean loop;
    private boolean running;
    private long lastUpdateTime;
    private long frameDuration;

    public SpriteAnimation() {
        sprites = new RectShort[0];
        currentFrameIndex = 0;
        loop = false;
        running = false;
        lastUpdateTime = now();
        setFrameTicks(1);
    }

    public void setContainer(SpriteAnimationSet container) {
        this.container = Objects.requireNonNull(container);
    }

    public void update(SpriteAnimationSet container, long now) {
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
            if (container != null) {
                container.register(this);
            }
            running = true;
            lastUpdateTime = now();
        }
    }

    public void stop() {
        if (container != null) {
            container.unregister(this);
        }
        running = false;
    }

    public void reset() {
        stop();
        currentFrameIndex = 0;
        lastUpdateTime = now();
    }

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

    public void setSprites(RectShort[] sprites) {
        if (this.sprites == sprites) {
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
        frameDuration = 1_000_000_000L / FRAME_RATE * numTicks;
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

    private boolean isValidFrame(int index) { return 0 <= index && index < sprites.length; }

    private long now() {
        return System.nanoTime();
    }
}