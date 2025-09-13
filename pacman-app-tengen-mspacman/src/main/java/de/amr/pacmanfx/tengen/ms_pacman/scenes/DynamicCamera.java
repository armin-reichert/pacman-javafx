/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import javafx.scene.ParallelCamera;

import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

/**
 * Camera used in 2D play scene.
 */
class DynamicCamera extends ParallelCamera {

    private static final double MIN_CAMERA_MOVEMENT = 0.5;
    private static final float SPEED = 0.02f;

    boolean followTarget;
    int idleTicks;
    double targetY;
    double minY;
    double maxY;

    public DynamicCamera() {
        followTarget = false;
    }

    public void update(double frac) {
        if (idleTicks > 0) {
            --idleTicks;
            return;
        }
        if (followTarget) {
            // Move fast when targeting top or bottom of maze
            if (frac < 0.4) {
                frac = 0;
            } else if (frac > 0.6) {
                frac = 1f;
            }
            targetY = lerp(minY, maxY, frac);
        }
        double oldY = getTranslateY();
        double newY = lerp(oldY, targetY, SPEED);
        newY = Math.clamp(newY, minY, maxY);
        if (Math.abs(oldY - newY) > MIN_CAMERA_MOVEMENT) {
            setTranslateY(newY);
        }
    }

    public void moveTop() {
        setTranslateY(minY);
    }

    public void targetTop() {
        followTarget = false;
        targetY = minY;
    }

    public void targetBottom() {
        followTarget = false;
        targetY = maxY;
    }
}