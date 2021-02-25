package de.amr.games.pacman.ui.fx.input;

import java.util.BitSet;

import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Keyboard input for a scene.
 * 
 * @author Armin Reichert
 */
public class Keyboard {

	private final BitSet pressedKeys = new BitSet(256);

	public Keyboard(Scene scene) {
		scene.setOnKeyPressed(e -> {
			if (e.getCode().getCode() != 0 && e.getCode().getCode() < 256) {
				pressedKeys.set(e.getCode().getCode());
			}
			controlCamera(scene, e);
		});
		scene.setOnKeyReleased(e -> {
			pressedKeys.clear(e.getCode().getCode());
		});
	}

	public boolean keyPressed(String keySpec) {
		return pressedKeys.get(keyCode(keySpec));
	}

	public void clearKey(String keySpec) {
		pressedKeys.clear(keyCode(keySpec));
	}

	private int keyCode(String keySpec) {
		KeyCode keyCode = KeyCode.getKeyCode(keySpec);
		if (keyCode != null) {
			return keyCode.getCode();
		}
		throw new IllegalArgumentException(String.format("Unkown key specification: '%s'", keySpec));
	}

	private void controlCamera(Scene scene, KeyEvent e) {
		Camera camera = scene.getCamera();
		if (camera == null) {
			return;
		}
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				camera.setTranslateX(0);
				camera.setTranslateY(0);
				camera.setTranslateZ(0);
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
			case LEFT:
				camera.setRotate(camera.getRotate() - 10);
				break;
			case RIGHT:
				camera.setRotate(camera.getRotate() + 10);
				break;
			default:
				break;
			}
		}
	}
}