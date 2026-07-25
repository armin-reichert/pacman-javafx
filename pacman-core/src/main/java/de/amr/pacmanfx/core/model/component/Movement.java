/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component;

public class Movement implements EntityComponent {

    public float velX;
    public float velY;

    public float accX;
    public float accY;

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

    public void move(Position position) {
        position.x += velX;
        position.y += velY;
        velX += accX;
        velY += accY;
    }
}
