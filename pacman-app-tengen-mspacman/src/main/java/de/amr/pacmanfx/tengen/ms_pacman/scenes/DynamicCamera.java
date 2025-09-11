/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.ParallelCamera;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

/**
 * Camera used in 2D play scene.
 */
public class DynamicCamera extends ParallelCamera {

    private static final double MIN_CAMERA_MOVEMENT = 0.5;
    private static final float SPEED = 0.02f;

    private final FloatProperty scaling = new SimpleFloatProperty(1);

    private int idleTicks;
    private int verticalRangeInTiles;
    private double targetY;
    private boolean followTarget;

    public void update(Actor target) {
        if (idleTicks > 0) {
            --idleTicks;
            return;
        }
        if (followTarget) {
            double frac = (float) target.tile().y() / verticalRangeInTiles;
            if (frac < 0.4) {
                frac = 0;
            } else if (frac > 0.6) {
                frac = 1f;
            }
            targetY = lerp(minY(), maxY(), frac);
        }
        double camY = lerp(getTranslateY(), targetY, SPEED);
        camY = Math.clamp(camY, minY(), maxY());
        if (Math.abs(getTranslateY() - camY) > MIN_CAMERA_MOVEMENT) {
            setTranslateY(camY);
        }
        Logger.debug("Camera: y={0.00} target={} top={} bottom={}", getTranslateY(), targetY, minY(), maxY());
    }

    public void moveTop() {
        setTranslateY(minY());
    }

    public void targetTop() {
        followTarget = false;
        targetY = minY();
    }

    public void targetBottom() {
        followTarget = false;
        targetY = maxY();
    }

    //TODO clarify
    public double minY() {
        double s = scaling();
        return -9 * TS * s;
    }

    //TODO clarify
    public double maxY() {
        double s = scaling();
        return (verticalRangeInTiles - 34) * TS * s;
    }

    public FloatProperty scalingProperty() {
        return scaling;
    }

    public float scaling() { return scaling.get(); }

    public void setIdleTime(int ticks) {
        idleTicks = ticks;
    }

    public void setVerticalRangeInTiles(int numTiles) {
        verticalRangeInTiles = numTiles;
    }

    public void setFollowTarget(boolean focus) {
        followTarget = focus;
    }

}
