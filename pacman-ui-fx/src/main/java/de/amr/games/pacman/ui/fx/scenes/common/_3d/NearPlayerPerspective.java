package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

public class NearPlayerPerspective implements PlayScenePerspective {

	private final Camera camera;

	public NearPlayerPerspective(Camera camera) {
		this.camera = camera;
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	public void reset() {
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(60);
		camera.setTranslateZ(-60);
	}

	@Override
	public void follow(Node target) {
		camera.setTranslateX(approach(camera.getTranslateX(), target.getTranslateX() - 100));
		camera.setTranslateY(approach(camera.getTranslateY(), target.getTranslateY()));
	}

	@Override
	public String toString() {
		return "Near Player";
	}
}