/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class CamTotal implements CameraController {

    public final IntegerProperty PY_ROTATE = new SimpleIntegerProperty(66);
    public final IntegerProperty PY_X = new SimpleIntegerProperty(0);
    public final IntegerProperty PY_Y = new SimpleIntegerProperty(330);
    public final IntegerProperty PY_Z = new SimpleIntegerProperty(-140);

    @Override
    public void reset(Camera cam) {
        cam.setNearClip(0.1);
        cam.setFarClip(10000.0);
        cam.setRotationAxis(Rotate.X_AXIS);
    }

    @Override
    public void update(Camera cam, Pac3D pac3D) {
        // cam properties cannot be bound and be set at the same time
        cam.rotateProperty().set(PY_ROTATE.get());
        cam.translateXProperty().set(PY_X.get());
        cam.translateYProperty().set(PY_Y.get());
        cam.translateZProperty().set(PY_Z.get());
    }

    @Override
    public String toString() {
        return "Total";
    }
}