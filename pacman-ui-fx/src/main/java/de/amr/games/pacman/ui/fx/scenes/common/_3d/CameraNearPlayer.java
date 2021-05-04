package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class CameraNearPlayer extends PlaySceneCamera {

	protected CameraController cameraController;

	public CameraNearPlayer(SubScene scene) {
		cameraController = new CameraController(this);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
	}

	@Override
	public void reset() {
		setNearClip(0.1);
		setFarClip(10000.0);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(60);
		setTranslateZ(-60);
	}

	@Override
	public void follow(Node target) {
		setTranslateX(lerp(getTranslateX(), target.getTranslateX() - 100));
		setTranslateY(lerp(getTranslateY(), target.getTranslateY()));
	}

	@Override
	public String toString() {
		return "Near Player";
	}
}