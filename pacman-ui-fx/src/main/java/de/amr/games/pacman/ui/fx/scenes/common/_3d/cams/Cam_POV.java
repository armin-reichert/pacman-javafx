package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D_Raw;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3DPerspective;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * Point Of View perspective.
 * 
 * @author Armin Reichert
 */
public class Cam_POV implements PlayScene3DPerspective {

	private final PlayScene3D_Raw playScene;

	public Cam_POV(PlayScene3D_Raw playScene) {
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
	public void follow(Player3D player3D) {
		Camera camera = playScene.getSubSceneFX().getCamera();
		V2d offset = new V2d(player3D.player.dir().vec).scaled(8);
		camera.setRotationAxis(Rotate.Z_AXIS);
		camera.setRotate(player3D.getRotate());
		camera.setTranslateX(-14 * 8 + player3D.player.position().x + offset.x);
		camera.setTranslateY(-18 * 8 + player3D.player.position().y + offset.y);
	}

	@Override
	public String toString() {
		return "POV";
	}
}