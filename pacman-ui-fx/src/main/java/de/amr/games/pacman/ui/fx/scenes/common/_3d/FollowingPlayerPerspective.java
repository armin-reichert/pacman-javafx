package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

public class FollowingPlayerPerspective implements PlayScenePerspective {

	private final Camera camera;

	public FollowingPlayerPerspective(Camera camera) {
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
		camera.setRotate(30);
		camera.setTranslateZ(-250);
	}

	@Override
	public void follow(Node target) {
		camera.setTranslateX(Math.min(10, approach(camera.getTranslateX(), target.getTranslateX())));
		camera.setTranslateY(Math.max(50, approach(camera.getTranslateY(), target.getTranslateY())));
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}