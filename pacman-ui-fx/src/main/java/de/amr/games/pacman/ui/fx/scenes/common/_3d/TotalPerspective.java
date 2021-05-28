package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Shows a total of the complete game. Using the keyboard, can zoom in and out and change angle.
 * 
 * @author Armin Reichert
 */
public class TotalPerspective implements PlayScenePerspective {

	private final SubScene subScene;
	private ManualCameraController cameraController;

	public TotalPerspective(SubScene subScene) {
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
	public void follow(Node target) {
	}

	@Override
	public String toString() {
		return "Total";
	}
}