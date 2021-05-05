package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class CameraNearPlayer extends PlaySceneCamera {

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
	public void handle(KeyEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "Near Player";
	}
}