/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class CamTotal implements CameraController {

	@Override
	public void reset(Camera cam) {
		cam.setNearClip(0.1);
		cam.setFarClip(10000.0);
		cam.setTranslateX(0);
		cam.setTranslateY(340);
		cam.setTranslateZ(-180);
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(60);
	}

	@Override
	public String toString() {
		return "Total";
	}
}