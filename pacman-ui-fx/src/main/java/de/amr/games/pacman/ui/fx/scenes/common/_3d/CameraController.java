package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Controls a perspective camera using keys.
 * 
 * @author Armin Reichert
 */
public class CameraController {

	private Camera camera;

	public CameraController(Camera camera) {
		this.camera = camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public void handleKeyEvent(KeyEvent e) {
		if (camera == null) {
			return;
		}
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				camera.setTranslateX(0);
				camera.setTranslateY(0);
				camera.setTranslateZ(-300);
				camera.setRotationAxis(Rotate.X_AXIS);
				camera.setRotate(0);
				camera.setRotationAxis(Rotate.Y_AXIS);
				camera.setRotate(0);
				camera.setRotationAxis(Rotate.Z_AXIS);
				camera.setRotate(0);
				break;
			case LEFT:
				camera.setTranslateX(camera.getTranslateX() + 10);
				break;
			case RIGHT:
				camera.setTranslateX(camera.getTranslateX() - 10);
				break;
			case UP:
				camera.setTranslateY(camera.getTranslateY() + 10);
				break;
			case DOWN:
				camera.setTranslateY(camera.getTranslateY() - 10);
				break;
			case PLUS:
				camera.setTranslateZ(camera.getTranslateZ() + 10);
				break;
			case MINUS:
				camera.setTranslateZ(camera.getTranslateZ() - 10);
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				camera.setRotationAxis(Rotate.X_AXIS);
				camera.setRotate((360 + camera.getRotate() - 1) % 360);
				break;
			case UP:
				camera.setRotationAxis(Rotate.X_AXIS);
				camera.setRotate((camera.getRotate() + 1) % 360);
				break;
			case LEFT:
				camera.setRotationAxis(Rotate.Z_AXIS);
				camera.setRotate((360 + camera.getRotate() - 1) % 360);
				break;
			case RIGHT:
				camera.setRotationAxis(Rotate.Z_AXIS);
				camera.setRotate((360 + camera.getRotate() + 1) % 360);
				break;
			default:
				break;
			}
		}
	}
}