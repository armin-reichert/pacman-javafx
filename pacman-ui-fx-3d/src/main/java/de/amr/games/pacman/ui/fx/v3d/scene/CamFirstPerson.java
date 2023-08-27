/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.HTS;

/**
 * @author Armin Reichert
 */
public class CamFirstPerson implements CameraController {

	private Pac pac;

	public void setPac(Pac pac) {
		this.pac = pac;
	}

	@Override
	public String toString() {
		return "Near Player";
	}

	@Override
	public void reset(Camera cam) {
		cam.setNearClip(0.1);
		cam.setFarClip(1000.0);
		cam.getTransforms().clear();
		cam.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		cam.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
	}

	@Override
	public void update(Camera cam, Pac3D pac3D) {
		var ref = pac.position().plus(pac.moveDir().vector().toFloatVec().scaled(6));
		cam.setTranslateX(ref.x() - ArcadeWorld.TILES_X * HTS);
		cam.setTranslateY(ref.y() - ArcadeWorld.TILES_Y * HTS);
		cam.setTranslateZ(-6);
		cam.setRotationAxis(Rotate.Z_AXIS);
		cam.setRotate(rotate(pac.moveDir()));
	}

	private double rotate(Direction dir) {
		return switch (dir) {
		case LEFT -> 180;
		case RIGHT -> 0;
		case UP -> 270;
		case DOWN -> 90;
		};
	}
}