package de.amr.games.pacman.ui.fx.scenes.common._3d;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Point Of View perspective.
 * 
 * @author Armin Reichert
 */
public class POVPerspective implements PlayScenePerspective {

	private final PlayScene3DBase playScene;

	public POVPerspective(PlayScene3DBase playScene) {
		this.playScene = playScene;
	}

	@Override
	public void reset() {
		Camera camera = playScene.getSubSceneFX().getCamera();
		camera.setNearClip(0.001);
		camera.setFarClip(100);
		camera.setTranslateZ(-40);
	}

	@Override
	public void follow(Node target) {
		Camera camera = playScene.getSubSceneFX().getCamera();
		Pac pac = playScene.player3D.pac;
		V2d offset = new V2d(pac.dir().vec).scaled(8);
		camera.setRotationAxis(Rotate.Z_AXIS);
		camera.setRotate(playScene.player3D.getRotate());
		camera.setTranslateX(-14 * 8 + pac.position().x + offset.x);
		camera.setTranslateY(-18 * 8 + pac.position().y + offset.y);
	}

	@Override
	public String toString() {
		return "POV";
	}
}