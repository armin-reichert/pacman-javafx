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
	 * If the event is not yet consumed, it is stored, otherwise it is discarded.
	 * 
	 * @param e key event
	 */
	public static void handleKeyEvent(KeyEvent e) {
		if (e.isConsumed()) {
			currentKeyEvent = null;
			Logger.trace("Discarded key event ({}): {}", e.getCode(), e);
		} else {
			currentKeyEvent = e;
			e.consume();
			Logger.trace("Stored and consumed key event ({}): {}", e.getCode(), e);
		}
	}

	public static void clearState() {
		currentKeyEvent = null;
	}

	public static boolean pressed(KeyCodeCombination... combinations) {
		if (combinations.length == 0) {
			return false;
		}
		if (combinations.length == 1) {
			return keyPressed(combinations[0]);
		}
		return Stream.of(combinations).anyMatch(Keyboard::keyPressed);
	}

	private static boolean keyPressed(KeyCodeCombination combination) {
		if (currentKeyEvent == null) {
			return false;
		}
		boolean matching = combination.match(currentKeyEvent);
		if (matching) {
			Logger.trace("Key event matches combination {}", combination.getName());
		}
		return matching;
	}

	private Keyboard() {
	}
}