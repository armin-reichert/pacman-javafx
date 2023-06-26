/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class Keyboard {

	public static KeyCodeCombination just(KeyCode code) {
		return new KeyCodeCombination(code);
	}

	public static KeyCodeCombination alt(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
	}

	public static KeyCodeCombination shift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
	}

	private static KeyEvent currentKeyEvent;

	/**
	 * If the event is not yet consumed, it is stored and can be matched against key combinations.
	 * 
	 * @param e key event
	 */
	public static void accept(KeyEvent e) {
		if (e.isConsumed()) {
			currentKeyEvent = null;
			Logger.trace("Ignored key event ({}): {}", e.getCode(), e);
		} else {
			currentKeyEvent = e;
			e.consume();
			Logger.trace("Consumed key event ({}): {}", e.getCode(), e);
		}
	}

	public static boolean anyPressed(KeyCodeCombination... combinations) {
		return Stream.of(combinations).anyMatch(Keyboard::pressed);
	}

	public static boolean pressed(KeyCodeCombination combination) {
		if (currentKeyEvent == null) {
			return false;
		}
		var match = combination.match(currentKeyEvent);
		if (match) {
			Logger.trace("Key event matches combination {}", combination.getName());
		}
		return match;
	}

	public static void clearState() {
		currentKeyEvent = null;
	}

	private Keyboard() {
	}
}