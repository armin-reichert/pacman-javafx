package de.amr.games.pacman.ui.fx.input;

import java.util.BitSet;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Keyboard input for a scene.
 * 
 * @author Armin Reichert
 */
public class Keyboard {

	private final BitSet pressedKeys = new BitSet(256);

	public void onKeyPressed(KeyEvent e) {
		if (e.isAltDown() || e.isControlDown() || e.isShiftDown()) {
			return;
		}
		if (e.getCode().getCode() != 0 && e.getCode().getCode() < 256) {
			pressedKeys.set(e.getCode().getCode());
		}
	}

	public void onKeyReleased(KeyEvent e) {
		pressedKeys.clear(e.getCode().getCode());
	}

	public boolean keyPressed(String keySpec) {
		boolean pressed = pressedKeys.get(keyCode(keySpec));
		clearKey(keySpec);
		return pressed;
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
}