/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class CamTotal implements CameraController {

    public static final IntegerProperty PY_ROTATE = new SimpleIntegerProperty(66);
    public static final IntegerProperty PY_X = new SimpleIntegerProperty(0);
    public static final IntegerProperty PY_Y = new SimpleIntegerProperty(330);
    public static final IntegerProperty PY_Z = new SimpleIntegerProperty(-140);

    @Override
    public void reset(Camera cam) {
        cam.setNearClip(0.1);
        cam.setFarClip(10000.0);
        cam.setRotationAxis(Rotate.X_AXIS);
        cam.rotateProperty().bind(PY_ROTATE);
        cam.translateXProperty().bind(PY_X);
        cam.translateYProperty().bind(PY_Y);
        cam.translateZProperty().bind(PY_Z);
    }

    @Override
    public String toString() {
        return "Total";
    }
}