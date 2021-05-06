package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.transform.Rotate;

public class FollowingPlayerPerspective implements PlayScenePerspective {

	private final SubScene subScene;

	public FollowingPlayerPerspective(SubScene subScene) {
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
	public void follow(Node target) {
		Camera camera = subScene.getCamera();
		camera.setTranslateX(Math.min(10, approach(camera.getTranslateX(), target.getTranslateX())));
		camera.setTranslateY(Math.max(50, approach(camera.getTranslateY(), target.getTranslateY())));
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}