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

package de.amr.games.pacman.ui.fx.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author Armin Reichert
 */
public class Keyboard {

	public static boolean pressed(KeyCode code) {
		return pressed(Modifier.NO_MODIFIER, code);
	}

	public static boolean pressed(Modifier modifier, KeyCode code) {
		return KB.isPressed(modifier.mask, code);
	}

	public static void clear() {
		KB.doClear();
	}

	public static void processEvent(KeyEvent e) {
		KB.doProcessEvent(e);
	}

	public static void addHandler(Runnable handler) {
		KB.handlers.add(handler);
	}

	public static void removeHandler(Runnable handler) {
		KB.handlers.remove(handler);
	}

	// Internal

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final Keyboard KB = new Keyboard();

	private KeyEvent currentEvent;
	private byte currentMask;
	private final List<Runnable> handlers = new ArrayList<>();

	private Keyboard() {
	}

	private static String modifierText(byte mask) {
		if (mask == Modifier.NO_MODIFIER.mask) {
			return "(NO MODIFIER) ";
		}
		String text = "";
		if ((mask & Modifier.ALT.mask) != 0) {
			text += "ALT";
		}
		if ((mask & Modifier.CTRL.mask) != 0) {
			text += " CONTROL";
		}
		if ((mask & Modifier.SHIFT.mask) != 0) {
			text += " SHIFT";
		}
		return (text + "+").trim();
	}

	private void doClear() {
		currentEvent = null;
		currentMask = 0;
	}

	private void doProcessEvent(KeyEvent e) {
		if (e.isConsumed()) {
			return;
		}
		currentEvent = e;
		currentMask = Modifier.NO_MODIFIER.mask;
		if (e.isAltDown()) {
			currentMask |= Modifier.ALT.mask;
		}
		if (e.isControlDown()) {
			currentMask |= Modifier.CTRL.mask;
		}
		if (e.isShiftDown()) {
			currentMask |= Modifier.SHIFT.mask;
		}
		handlers.forEach(Runnable::run);
		e.consume();
	}

	private boolean isPressed(int modifierMask, KeyCode code) {
		if (currentEvent != null && currentEvent.getCode() == code && currentMask == modifierMask) {
			LOGGER.trace(() -> "Key press handled: %s%s".formatted(modifierText(currentMask), code));
			currentEvent.consume();
			return true;
		}
		return false;
	}
}