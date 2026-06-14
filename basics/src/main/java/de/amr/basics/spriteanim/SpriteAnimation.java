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

    private SpriteAnimationContainer container;

    private RectShort[] sprites;
    private int currentFrameIndex;
    private boolean loop;
    private boolean running;

    private int frameDurationTicks;
    private int smallTick;

    public SpriteAnimation() {
        sprites = new RectShort[0];
        currentFrameIndex = 0;
        loop = false;
        running = false;
        smallTick = 0;
        setFrameDurationTicks(1);
    }

    public void setContainer(SpriteAnimationContainer container) {
        this.container = Objects.requireNonNull(container);
    }

    public void tick() {
        if (!running) {
            return;
        }
        if (smallTick == frameDurationTicks - 1) {
            advanceFrame();
        }
        smallTick = (smallTick + 1) % frameDurationTicks;
    }

    public void start() {
        if (!running) {
            if (container != null) {
                container.add(this);
            }
            running = true;
        }
    }

    public void stop() {
        if (container != null) {
            container.remove(this);
        }
        running = false;
    }

    public void reset() {
        stop();
        currentFrameIndex = 0;
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

    public void setFrameDurationTicks(int numTicks) {
        if (numTicks <= 0) {
            throw new IllegalArgumentException("Frame ticks must be a positive number, but you gave me " + numTicks);
        }
        frameDurationTicks = numTicks;
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
}