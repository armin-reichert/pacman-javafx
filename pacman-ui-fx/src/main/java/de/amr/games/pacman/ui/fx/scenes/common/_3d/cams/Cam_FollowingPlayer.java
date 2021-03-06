package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlaySceneCam;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * Follows the player but still shows a larger portion of the maze.
 * 
 * @author Armin Reichert
 */
public class Cam_FollowingPlayer implements PlaySceneCam {

	private final Camera cam;

	public Cam_FollowingPlayer(Camera cam) {
		this.cam = cam;
	}

	@Override
	public void reset() {
		cam.setNearClip(0.1);
		cam.setFarClip(10000.0);
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(30);
		cam.setTranslateZ(-250);
	}

	@Override
	public void follow(Player3D player3D) {
		cam.setTranslateX(Math.min(10, approach(cam.getTranslateX(), player3D.getTranslateX())));
		cam.setTranslateY(Math.max(50, approach(cam.getTranslateY(), player3D.getTranslateY())));
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}