package de.amr.games.pacman.ui.fx.util;

import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Controls a camera using keyboard events.
 * 
 * @author Armin Reichert
 */
public class ManualCameraController {

	protected final Camera cam;

	public ManualCameraController(Camera cam) {
		this.cam = cam;
	}

	public void handleKeyEvent(KeyEvent e) {
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				cam.setTranslateX(0);
				cam.setTranslateY(0);
				cam.setTranslateZ(-300);
				cam.setRotationAxis(Rotate.X_AXIS);
				cam.setRotate(0);
				cam.setRotationAxis(Rotate.Y_AXIS);
				cam.setRotate(0);
				cam.setRotationAxis(Rotate.Z_AXIS);
				cam.setRotate(0);
				break;
			case LEFT:
				cam.setTranslateX(cam.getTranslateX() + 10);
				break;
			case RIGHT:
				cam.setTranslateX(cam.getTranslateX() - 10);
				break;
			case UP:
				cam.setTranslateY(cam.getTranslateY() + 10);
				break;
			case DOWN:
				cam.setTranslateY(cam.getTranslateY() - 10);
				break;
			case PLUS:
				cam.setTranslateZ(cam.getTranslateZ() + 10);
				break;
			case MINUS:
				cam.setTranslateZ(cam.getTranslateZ() - 10);
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				cam.setRotationAxis(Rotate.X_AXIS);
				cam.setRotate((360 + cam.getRotate() - 1) % 360);
				break;
			case UP:
				cam.setRotationAxis(Rotate.X_AXIS);
				cam.setRotate((cam.getRotate() + 1) % 360);
				break;
			case LEFT:
				cam.setRotationAxis(Rotate.Z_AXIS);
				cam.setRotate((360 + cam.getRotate() - 1) % 360);
				break;
			case RIGHT:
				cam.setRotationAxis(Rotate.Z_AXIS);
				cam.setRotate((360 + cam.getRotate() + 1) % 360);
				break;
			default:
				break;
			}
		}
	}
}