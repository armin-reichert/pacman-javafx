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
	private static Runnable callback;

	public static void handleKeyEvent(KeyEvent e) {
		if (e.isConsumed()) {
			LOG.trace("Keyboard: Key event ignored (already consumed): %s %s", e.getCode(), e);
			return;
		}
		currentEvent = e;
		e.consume();
		LOG.trace("Keyboard: Key event consumed: %s %s", e.getCode(), e);
		if (callback != null) {
			callback.run();
		}
	}

	public static void setCallback(Runnable callback) {
		Keyboard.callback = callback;
	}

	public static boolean pressed(KeyCodeCombination kcc) {
		return currentEvent != null && kcc.match(currentEvent);
	}

	public static void clear() {
		currentEvent = null;
	}

	private Keyboard() {
	}
}