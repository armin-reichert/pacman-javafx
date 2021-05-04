package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class CameraFollowingPlayer extends PlaySceneCamera {

	protected CameraController cameraController;

	public CameraFollowingPlayer(SubScene scene) {
		cameraController = new CameraController(this);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
	}

	@Override
	public void reset() {
		setNearClip(0.1);
		setFarClip(10000.0);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(30);
		setTranslateZ(-250);
	}

	@Override
	public void follow(Node target) {
		setTranslateX(Math.min(10, lerp(getTranslateX(), target.getTranslateX())));
		setTranslateY(Math.max(50, lerp(getTranslateY(), target.getTranslateY())));
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}