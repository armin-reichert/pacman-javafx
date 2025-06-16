/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

/**
 * Message starts at house center, moves to the right maze border, wraps around and moves to the house center again.
 */
public class MessageMovement {

    private float startX;
    private float wrappingX;
    private float speed;
    private float currentX;
    private boolean wrapped;
    private long delayBeforeMoving;
    private boolean running;

    public void start(long delay, float startX, float wrappingX) {
        this.startX = startX;
        this.wrappingX = wrappingX;
        this.speed = 1; //TODO how fast exactly?
        currentX = startX;
        wrapped = false;
        delayBeforeMoving = delay; //TODO how long exactly?
        running = true;
    }

    public void update() {
        if (!running) {
            return;
        }
        if (delayBeforeMoving > 0) {
            --delayBeforeMoving;
            return;
        }
        currentX += speed;
        if (currentX > wrappingX) {
            currentX = 0;
            wrapped = true;
        }
        if (wrapped && currentX >= startX) {
            speed = 0;
            currentX = startX;
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * @return current move position (x), message is centered horizontally over this position
     */
    public float currentX() {
        return currentX;
    }
}