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
		keySpec = keySpec.toLowerCase();
		if (keySpec.length() == 1) {
			int c = keySpec.charAt(0);
			int index = "abcdefghijklmnopqrstuvwxyz".indexOf(c);
			if (index != -1) {
				return KeyCode.A.getCode() + index;
			}
			index = "0123456789".indexOf(c);
			if (index != -1) {
				return KeyCode.DIGIT0.getCode() + index;
			}
		}
		switch (keySpec) {
		case "up":
			return KeyCode.UP.getCode();
		case "down":
			return KeyCode.DOWN.getCode();
		case "left":
			return KeyCode.LEFT.getCode();
		case "right":
			return KeyCode.RIGHT.getCode();
		case "escape":
			return KeyCode.ESCAPE.getCode();
		case "space":
			return KeyCode.SPACE.getCode();
		case "plus":
		case "+":
			return KeyCode.PLUS.getCode();
		case "minus":
		case "-":
			return KeyCode.MINUS.getCode();
		default:
			throw new IllegalArgumentException(String.format("Unknown key specification: %s", keySpec));
		}
	}
}