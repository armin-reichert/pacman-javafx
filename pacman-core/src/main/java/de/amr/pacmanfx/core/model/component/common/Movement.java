/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.common;

import de.amr.pacmanfx.core.model.component.EntityComponent;

public class Movement implements EntityComponent {

    public float velX;
    public float velY;

    public float accX;
    public float accY;

    @Override
    public void reset() {
        velX = 0;
        velY = 0;
        accX = 0;
        accY = 0;
    }

    public final void setVelX(double velX) {
        this.velX = (float) velX;
    }

    public final void setVelY(double velY) {
        this.velY = (float) velY;
    }

    public final void setVelocity(double vx, double vy) {
        this.velX = (float) vx;
        this.velY = (float) vy;
    }

    public double computeSpeed() {
        return Math.hypot(velX, velY);
    }

    public final void setAccX(double accX) {
        this.accX = (float) accX;
    }

    public final void setAccY(double accY) {
        this.accY = (float) accY;
    }

    public final void setAcceleration(double ax, double ay) {
        this.accX = (float) ax;
        this.accY = (float) ay;
    }

    @Override
    public String toString() {
        return "Movement{" +
            "velX=" + velX +
            ", velY=" + velY +
            ", accX=" + accX +
            ", accY=" + accY +
            '}';
    }
}
