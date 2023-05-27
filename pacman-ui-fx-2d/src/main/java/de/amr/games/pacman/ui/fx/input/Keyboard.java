/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.input;

import java.util.stream.Stream;

import org.tinylog.Logger;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

/**
 * @author Armin Reichert
 */
public class Keyboard {

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