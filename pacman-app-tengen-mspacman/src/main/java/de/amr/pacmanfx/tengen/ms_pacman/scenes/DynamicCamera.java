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
 * Camera used in play scene 2D.
 */
public class DynamicCamera extends ParallelCamera {

    private static final double MIN_CAMERA_MOVEMENT = 0.5;

    private final FloatProperty scalingPy = new SimpleFloatProperty(1);
    private int idleTicks;
    private int verticalRangeInTiles;
    private double targetY;
    private boolean focussingActor;
    private float speed = 0.02f;

    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    public void setIdleTime(int ticks) {
        idleTicks = ticks;
    }

    public void setVerticalRangeInTiles(int numTiles) {
        verticalRangeInTiles = numTiles;
    }

    public void setFocussingActor(boolean focus) {
        focussingActor = focus;
    }

    public void setCameraTopOfScene() {
        setTranslateY(camMinY());
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void moveTop() {
        focussingActor = false;
        targetY = camMinY();
    }

    public void moveBottom() {
        focussingActor = false;
        targetY = camMaxY();
    }

    public double camMinY() {
        return scalingProperty().get() * (-9 * TS);
    }

    public double camMaxY() {
        return scalingProperty().get() * (verticalRangeInTiles - 35) * TS;
    }

    public void update(Actor actor) {
        if (idleTicks > 0) {
            --idleTicks;
            return;
        }
        if (focussingActor) {
            float frac = (float) actor.tile().y() / verticalRangeInTiles;
            if (frac < 0.4) {
                frac = 0;
            } else if (frac > 0.6) {
                frac = 1f;
            }
            targetY = lerp(camMinY(), camMaxY(), frac);
        }
        double camY = lerp(getTranslateY(), targetY, speed);
        camY = Math.clamp(camY, camMinY(), camMaxY());
        if (Math.abs(getTranslateY() - camY) > MIN_CAMERA_MOVEMENT) {
            setTranslateY(camY);
        }
        Logger.debug("Camera: y={0.00} target={} top={} bottom={}", getTranslateY(), targetY, camMinY(), camMaxY());
    }
}
