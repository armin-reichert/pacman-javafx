package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScenePerspective;
import javafx.scene.Camera;
import javafx.scene.SubScene;
import javafx.scene.transform.Rotate;

/**
 * Follows the player but still shows a larger portion of the maze.
 * 
 * @author Armin Reichert
 */
public class Cam_FollowingPlayer implements PlayScenePerspective {

	private final SubScene subScene;

	public Cam_FollowingPlayer(SubScene subScene) {
		this.subScene = subScene;
	}

	@Override
	public void reset() {
		Camera camera = subScene.getCamera();
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);
		camera.setTranslateZ(-250);
	}

	@Override
	public void follow(Player3D player3D) {
		Camera camera = subScene.getCamera();
		camera.setTranslateX(Math.min(10, approach(camera.getTranslateX(), player3D.getTranslateX())));
		camera.setTranslateY(Math.max(50, approach(camera.getTranslateY(), player3D.getTranslateY())));
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}