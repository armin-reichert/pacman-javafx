package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScenePerspective;
import de.amr.games.pacman.ui.fx.util.ManualCameraController;
import javafx.scene.Camera;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Shows a total of the complete game. Using the keyboard, can zoom in and out and change angle.
 * 
 * @author Armin Reichert
 */
public class Cam_Total implements PlayScenePerspective {

	private final SubScene subScene;
	private ManualCameraController cameraController;

	public Cam_Total(SubScene subScene) {
		this.subScene = subScene;
		cameraController = new ManualCameraController(subScene.getCamera());
	}

	@Override
	public void handle(KeyEvent event) {
		cameraController.handleKeyEvent(event);
	}

	@Override
	public void reset() {
		Camera camera = subScene.getCamera();
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);
		camera.setTranslateX(0);
		camera.setTranslateY(270);
		camera.setTranslateZ(-460);
	}

	@Override
	public void follow(Player3D player3D) {
	}

	@Override
	public String toString() {
		return "Total";
	}
}