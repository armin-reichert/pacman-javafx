package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.transform.Rotate;

/**
 * Follows the player closely, board only partially visible.
 * 
 * @author Armin Reichert
 */
public class NearPlayerPerspective implements PlayScenePerspective {

	private final SubScene subScene;

	public NearPlayerPerspective(SubScene subScene) {
		this.subScene = subScene;
	}

	@Override
	public void reset() {
		Camera camera = subScene.getCamera();
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(60);
		camera.setTranslateZ(-60);
	}

	@Override
	public void follow(Node target) {
		Camera camera = subScene.getCamera();
		camera.setTranslateX(approach(camera.getTranslateX(), target.getTranslateX() - 100));
		camera.setTranslateY(approach(camera.getTranslateY(), target.getTranslateY()));
	}

	@Override
	public String toString() {
		return "Near Player";
	}
}