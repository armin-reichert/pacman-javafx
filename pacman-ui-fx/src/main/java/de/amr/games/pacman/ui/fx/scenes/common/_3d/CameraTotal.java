package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Node;
import javafx.scene.transform.Rotate;

public class CameraTotal extends PlaySceneCamera {

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