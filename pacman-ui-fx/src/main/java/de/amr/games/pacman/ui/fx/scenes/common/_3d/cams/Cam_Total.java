package de.amr.games.pacman.ui.fx.scenes.common._3d.cams;

import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlaySceneCam;
import de.amr.games.pacman.ui.fx.util.ManualCameraController;
import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Shows a total of the complete game. Using the keyboard, can zoom in and out and change angle.
 * 
 * @author Armin Reichert
 */
public class Cam_Total extends ManualCameraController implements PlaySceneCam {

	public Cam_Total(Camera cam) {
		super(cam);
	}

	@Override
	public void handle(KeyEvent event) {
		super.handleKeyEvent(event);
	}

	@Override
	public void reset() {
		cam.setNearClip(0.1);
		cam.setFarClip(10000.0);
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(30);
		cam.setTranslateX(0);
		cam.setTranslateY(270);
		cam.setTranslateZ(-460);
	}

	@Override
	public void follow(Player3D player3D) {
	}

	@Override
	public String toString() {
		return "Total";
	}
}