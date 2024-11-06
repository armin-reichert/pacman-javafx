/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

public class GameOverMessageAnimation {

    static final int DELAY_TICKS = 120; // TODO how long?
    static final float SPEED = 1;  // TODO how fast?

    private double startX;
    private double rightBorderX;
    private double speed;
    private double currentX;
    private boolean wrapped;
    private long delayBeforeMoving;

    public void start(double startX, double rightBorderX) {
        this.startX = startX;
        this.rightBorderX = rightBorderX;
        this.speed = SPEED;
        currentX = startX;
        wrapped = false;
        delayBeforeMoving = DELAY_TICKS;
    }

    public void update() {
        if (delayBeforeMoving > 0) {
            --delayBeforeMoving;
            return;
        }
        currentX += speed;
        if (currentX > rightBorderX) {
            currentX = 0;
            wrapped = true;
        }
        if (wrapped && currentX >= startX) {
            speed = 0;
            currentX = startX;
        }
    }

    public double currentX() {
        return currentX;
    }
}