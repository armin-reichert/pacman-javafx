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

    private final SpriteAnimationContainer container;

    private RectShort[] sprites;
    private int frame;
    private boolean loop;
    private boolean running;

    private int frameDurationTicks;
    private int smallTick;

    public SpriteAnimation(SpriteAnimationContainer container) {
        this.container = Objects.requireNonNull(container);

        sprites = new RectShort[0];
        frame = 0;
        loop = false;
        running = false;
        smallTick = 0;
        setFrameDurationTicks(1);
    }

    public void tick() {
        if (!running) {
            return;
        }
        if (smallTick == frameDurationTicks - 1) {
            advanceFrame();
            smallTick = 0;
        } else {
            ++smallTick;
        }
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
        frame = 0;
        smallTick = 0;
    }

    public void advanceFrame() {
        if (frame == sprites.length - 1) {
            if (loop) {
                frame = 0;
            } else {
                stop();
            }
        } else {
            ++frame;
        }
        smallTick = 0;
    }

    public void setSprites(RectShort[] sprites) {
        if (this.sprites == sprites) {
            return;
        }
        this.sprites = requireNonNull(sprites);
        if (sprites.length == 0) {
            throw new IllegalArgumentException("Sprites array is empty");
        }
        reset();
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

    public void setFrame(int index) {
        this.frame = requireValidFrame(index);
    }

    public int frame() { return frame; }

    public RectShort sprite() { return sprites[frame]; }

    private int requireValidFrame(int index) {
        if (0 <= index && index < sprites.length) {
            return index;
        }
        throw new IllegalArgumentException("Frame %d is out of range, number of sprites: %d".formatted(index, sprites.length));
    }
}