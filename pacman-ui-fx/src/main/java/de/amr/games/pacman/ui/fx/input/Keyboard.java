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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

/**
 * @author Armin Reichert
 */
public class Keyboard {

	public static boolean pressed(KeyCodeCombination kcc) {
		return THE_KEYBOARD.match(kcc);
	}

	public static void clear() {
		THE_KEYBOARD.doClear();
	}

	public static void processEvent(KeyEvent e) {
		THE_KEYBOARD.doProcessEvent(e);
	}

	public static void addHandler(Runnable handler) {
		THE_KEYBOARD.handlers.add(handler);
	}

	public static void removeHandler(Runnable handler) {
		THE_KEYBOARD.handlers.remove(handler);
	}

	// Internal

	private static final Logger LOG = LogManager.getFormatterLogger();
	private static final Keyboard THE_KEYBOARD = new Keyboard();

	private KeyEvent currentEvent;
	private final List<Runnable> handlers = new ArrayList<>();

	private Keyboard() {
	}

	private void doClear() {
		currentEvent = null;
	}

	private void doProcessEvent(KeyEvent e) {
		if (e.isConsumed()) {
			return;
		}
		currentEvent = e;
		handlers.forEach(Runnable::run);
		e.consume();
	}

	private boolean match(KeyCodeCombination combination) {
		if (currentEvent != null && combination.match(currentEvent)) {
			LOG.trace("Matching key code combination: %s", combination);
			currentEvent.consume();
			return true;
		}
		return false;
	}
}