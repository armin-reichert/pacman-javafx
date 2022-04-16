/**
 * 
 */
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.ui.fx.util.U.lerp;

import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Camera for the 3D play scene.
 * 
 * @author Armin Reichert
 */
public class PlaySceneCamera extends PerspectiveCamera {

	private Perspective perspective;

	public PlaySceneCamera() {
		super(true);
	}

	@Override
	public String toString() {
		return switch (perspective) {
		case CAM_DRONE -> "Drone";
		case CAM_FOLLOWING_PLAYER -> "Following Player";
		case CAM_NEAR_PLAYER -> "Near Player";
		case CAM_TOTAL -> "Total";
		};
	}

	public String transformInfo() {
		return String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", getTranslateX(), getTranslateY(), getTranslateZ(),
				getRotate());
	}

	public void setPerspective(Perspective perspective) {
		this.perspective = perspective;
		reset();
	}

	public void reset() {
		switch (perspective) {
		case CAM_DRONE -> {
			setNearClip(0.1);
			setFarClip(10000.0);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
			setTranslateX(0);
			setTranslateY(0);
			setTranslateZ(-400);
		}
		case CAM_FOLLOWING_PLAYER -> {
			setNearClip(0.1);
			setFarClip(10000.0);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(60);
			setTranslateZ(-160);
		}
		case CAM_NEAR_PLAYER -> {
			setNearClip(0.1);
			setFarClip(10000.0);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(80);
			setTranslateZ(-40);
		}
		case CAM_TOTAL -> {
			setNearClip(0.1);
			setFarClip(10000.0);
			setRotationAxis(Rotate.X_AXIS);
			setRotate(49);
			setTranslateX(0);
			setTranslateY(320);
			setTranslateZ(-260);
		}
		}
	}

	public void update(Pac3D player3D) {
		switch (perspective) {
		case CAM_DRONE -> {
			// TODO camera speed should be dependent from height over ground
			double speed = 0.005;
			double x = lerp(getTranslateX(), player3D.getTranslateX() - 100, speed);
			double y = lerp(getTranslateY(), player3D.getTranslateY() - 150, speed);
			setTranslateX(x);
			setTranslateY(y);
		}
		case CAM_FOLLOWING_PLAYER -> {
			// TODO this is just trial and error
			double speed = 0.03;
			double x = lerp(getTranslateX(), player3D.getTranslateX() - 100, speed);
			double y = lerp(getTranslateY(), player3D.getTranslateY() + 60, speed);
			setTranslateX(x);
			setTranslateY(y);
		}
		case CAM_NEAR_PLAYER -> {
			// TODO this is just trial and error
			double speed = 0.02;
			double x = lerp(getTranslateX(), player3D.getTranslateX() - 110, speed);
			double y = lerp(getTranslateY(), player3D.getTranslateY(), speed);
			setTranslateX(x);
			setTranslateY(y);
		}
		case CAM_TOTAL -> {
			// nothing to do
		}
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void onKeyPressed(KeyEvent e) {
		switch (perspective) {
		case CAM_DRONE -> {
			if (e.isAltDown()) {
				switch (e.getCode()) {
				case PLUS -> setTranslateZ(getTranslateZ() + 10);
				case MINUS -> setTranslateZ(getTranslateZ() - 10);
				}
			}
		}
		case CAM_FOLLOWING_PLAYER -> {
			// no keyboard
		}
		case CAM_NEAR_PLAYER -> {
			// no keyboard
		}
		case CAM_TOTAL -> {
			if (e.isAltDown()) {
				switch (e.getCode()) {
				case LEFT -> setTranslateX(getTranslateX() + 10);
				case RIGHT -> setTranslateX(getTranslateX() - 10);
				case UP -> setTranslateY(getTranslateY() + 10);
				case DOWN -> setTranslateY(getTranslateY() - 10);
				case PLUS -> setTranslateZ(getTranslateZ() + 10);
				case MINUS -> setTranslateZ(getTranslateZ() - 10);
				}
			} else if (e.isShiftDown()) {
				switch (e.getCode()) {
				case UP -> {
					setRotationAxis(Rotate.X_AXIS);
					setRotate((getRotate() - 1 + 360) % 360);
				}
				case DOWN -> {
					setRotationAxis(Rotate.X_AXIS);
					setRotate((getRotate() + 1 + 360) % 360);
				}
				}
			}
		}
		}
	}
}