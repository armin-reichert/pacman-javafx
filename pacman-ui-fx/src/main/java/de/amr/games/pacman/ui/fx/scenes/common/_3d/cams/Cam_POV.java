package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3DPerspective;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * Point Of View perspective.
 * 
 * @author Armin Reichert
 */
public class Cam_POV implements PlayScene3DPerspective {

	private final Camera cam;

	public Cam_POV(Camera cam) {
		this.cam = cam;
	}

	@Override
	public void reset() {
		cam.setNearClip(0.001);
		cam.setFarClip(100);
		cam.setTranslateZ(-40);
	}

	@Override
	public void follow(Player3D player3D) {
		V2d offset = new V2d(player3D.player.dir().vec).scaled(8);
		cam.setRotationAxis(Rotate.Z_AXIS);
		cam.setRotate(player3D.getRotate());
		cam.setTranslateX(-14 * 8 + player3D.player.position().x + offset.x);
		cam.setTranslateY(-18 * 8 + player3D.player.position().y + offset.y);
	}

	@Override
	public String toString() {
		return "POV";
	}
}