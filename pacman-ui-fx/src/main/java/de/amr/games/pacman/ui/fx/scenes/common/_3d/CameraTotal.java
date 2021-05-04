package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class CameraTotal extends PlaySceneCamera implements EventHandler<KeyEvent> {

	public final CameraController cameraController;

	public CameraTotal() {
		cameraController = new CameraController(this);
	}

	@Override
	public void handle(KeyEvent event) {
		if (event.getEventType() == KeyEvent.KEY_PRESSED) {
			cameraController.handleKeyEvent(event);
		}
	}

	@Override
	public void reset() {
		setNearClip(0.1);
		setFarClip(10000.0);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(30);
		setTranslateX(0);
		setTranslateY(270);
		setTranslateZ(-460);
	}

	@Override
	public void follow(Node target) {
	}

	@Override
	public String toString() {
		return "Total";
	}
}