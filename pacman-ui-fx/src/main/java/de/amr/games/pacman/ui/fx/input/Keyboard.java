package de.amr.games.pacman.ui.fx.input;

import java.util.BitSet;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

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
}