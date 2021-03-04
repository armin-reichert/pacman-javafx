package de.amr.games.pacman.ui.fx.common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Perspective camera that an be controlled using keys.
 * 
 * @author Armin Reichert
 */
public class ControllableCamera extends PerspectiveCamera {

	public StringProperty infoProperty = new SimpleStringProperty();

	public ControllableCamera() {
		setRotationAxis(Rotate.X_AXIS);
		// TODO is this the right way to do this?
		translateXProperty().addListener((source, oldValue, newValue) -> infoProperty.set(getInfo()));
		translateYProperty().addListener((source, oldValue, newValue) -> infoProperty.set(getInfo()));
		translateZProperty().addListener((source, oldValue, newValue) -> infoProperty.set(getInfo()));
		rotateProperty().addListener((source, oldValue, newValue) -> infoProperty.set(getInfo()));
	}

	public String getInfo() {
		return String.format("Camera: x=%.0f y=%.0f z=%.0f rot=%.0f", getTranslateX(), getTranslateY(), getTranslateZ(),
				getRotate());
	}

	public void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				setTranslateX(0);
				setTranslateY(0);
				setTranslateZ(0);
				setRotate(0);
				break;
			case LEFT:
				setTranslateX(getTranslateX() + 10);
				break;
			case RIGHT:
				setTranslateX(getTranslateX() - 10);
				break;
			case UP:
				setTranslateY(getTranslateY() + 10);
				break;
			case DOWN:
				setTranslateY(getTranslateY() - 10);
				break;
			case PLUS:
				setTranslateZ(getTranslateZ() + 10);
				break;
			case MINUS:
				setTranslateZ(getTranslateZ() - 10);
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				setRotate(getRotate() - 1);
				break;
			case UP:
				setRotate(getRotate() + 1);
				break;
			default:
				break;
			}
		}
	}
}