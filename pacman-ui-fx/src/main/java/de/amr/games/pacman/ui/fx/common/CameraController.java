package de.amr.games.pacman.ui.fx.common;

import java.util.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Controls a perspective camera using keys.
 * 
 * @author Armin Reichert
 */
public class CameraController {

	public StringProperty cameraInfoProperty = new SimpleStringProperty();

	private final Camera camera;

	public CameraController(Camera camera) {
		this.camera = Objects.requireNonNull(camera, "CameraController cannot be created for NULL camera");
		// TODO is this the right way to do this?
		camera.translateXProperty().addListener((s, o, n) -> cameraInfoProperty.set(computeCameraInfo()));
		camera.translateYProperty().addListener((s, o, n) -> cameraInfoProperty.set(computeCameraInfo()));
		camera.translateZProperty().addListener((s, o, n) -> cameraInfoProperty.set(computeCameraInfo()));
		camera.rotateProperty().addListener((s, o, n) -> cameraInfoProperty.set(computeCameraInfo()));
	}

	public String getCameraInfo() {
		return cameraInfoProperty.get();
	}

	public String computeCameraInfo() {
		return String.format("Camera: x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
				camera.getTranslateZ(), camera.getRotate());
	}

	public void handleKeyEvent(KeyEvent e) {
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				camera.setTranslateX(0);
				camera.setTranslateY(0);
				camera.setTranslateZ(0);
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
			default:
				break;
			}
		}
	}
}