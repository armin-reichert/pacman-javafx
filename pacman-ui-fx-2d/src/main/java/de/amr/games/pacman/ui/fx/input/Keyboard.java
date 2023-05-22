/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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