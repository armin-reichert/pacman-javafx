/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

/**
 * @author Armin Reichert
 */
public class Keyboard {

	private static final Logger LOG = LogManager.getFormatterLogger();
	private static KeyEvent currentEvent;

	/**
	 * If the event is not yet consumed, it is accepted as the current event and can then be queried by the application
	 * code.
	 * 
	 * @param e key event
	 * @return if the event was accepted
	 */
	public static boolean accept(KeyEvent e) {
		if (e.isConsumed()) {
			LOG.trace("Key event (%s) rejected: %s", e.getCode(), e);
			currentEvent = null;
			return false;
		}
		LOG.trace("Key event (%s) accepted: %s", e.getCode(), e);
		currentEvent = e;
		e.consume();
		return true;
	}

	public static boolean pressed(KeyCodeCombination combination) {
		return currentEvent != null && combination.match(currentEvent);
	}

	public static void clear() {
		currentEvent = null;
	}

	private Keyboard() {
	}
}