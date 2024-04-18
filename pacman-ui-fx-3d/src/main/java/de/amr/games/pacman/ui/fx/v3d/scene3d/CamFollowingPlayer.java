/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.lerp;

/**
 * @author Armin Reichert
 */
public class CamFollowingPlayer implements CameraController {

    @Override
    public String toString() {
        return "Following Player";
    }

    @Override
    public void reset(Camera cam) {
        cam.setNearClip(0.1);
        cam.setFarClip(10000.0);
        cam.setRotationAxis(Rotate.X_AXIS);
        cam.setRotate(60);
        cam.setTranslateZ(-160);
    }

    @Override
    public void update(Camera cam, Pac3D pac3D) {
        var position = pac3D.position();
        double speedX = 0.005;
        cam.setTranslateX(lerp(cam.getTranslateX(), position.getX() - 100, speedX));
        double speedY = 0.030;
        cam.setTranslateY(lerp(cam.getTranslateY(), position.getY() + 100, speedY));
    }
}