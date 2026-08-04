/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class MovementComp implements GameEntityComponent {

    private float vx;
    private float vy;

    private float ax;
    private float ay;

    @Override
    public void reset() {
        vx = 0;
        vy = 0;
        ax = 0;
        ay = 0;
    }

    public float velocityX() {
        return vx;
    }

    public final void setVelocityX(double vx) {
        this.vx = (float) vx;
    }

    public float velocityY() {
        return vy;
    }

    public final void setVelocityY(double vy) {
        this.vy = (float) vy;
    }

    public final void setVelocity(double vx, double vy) {
        this.vx = (float) vx;
        this.vy = (float) vy;
    }

    public float speed() {
        return (float) Math.hypot(vx, vy);
    }

    public float accelerationX() {
        return ax;
    }

    public final void setAccelerationX(double ax) {
        this.ax = (float) ax;
    }

    public float accelerationY() {
        return ay;
    }

    public final void setAccelerationY(double ay) {
        this.ay = (float) ay;
    }

    public final void setAcceleration(double ax, double ay) {
        this.ax = (float) ax;
        this.ay = (float) ay;
    }

    public void addVelocity(float vx, float vy) {
        this.vx += vx;
        this.vy += vy;
    }

    public boolean hasZeroVelocity() {
        return vx == 0 && vy == 0;
    }

    @Override
    public String toString() {
        return "Movement{" +
            "velocityX=" + vx +
            ", velocityY=" + vy +
            ", accelerationX=" + ax +
            ", accelerationY=" + ay +
            '}';
    }
}
