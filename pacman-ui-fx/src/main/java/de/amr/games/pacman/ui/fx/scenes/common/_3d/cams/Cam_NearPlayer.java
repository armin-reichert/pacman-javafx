package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlaySceneCam;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * Follows the player closely, board only partially visible.
 * 
 * @author Armin Reichert
 */
public class Cam_NearPlayer implements PlaySceneCam {

	private final Camera cam;

	public Cam_NearPlayer(Camera cam) {
		this.cam = cam;
	}

	@Override
	public void reset() {
		cam.setNearClip(0.1);
		cam.setFarClip(10000.0);
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(60);
		cam.setTranslateZ(-60);
	}

	@Override
	public void follow(Player3D player3D) {
		cam.setTranslateX(approach(cam.getTranslateX(), player3D.getTranslateX() - 100));
		cam.setTranslateY(approach(cam.getTranslateY(), player3D.getTranslateY()));
	}

	@Override
	public String toString() {
		return "Near Player";
	}
}