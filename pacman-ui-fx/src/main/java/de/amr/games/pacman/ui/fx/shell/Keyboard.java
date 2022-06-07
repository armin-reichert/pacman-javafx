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

package de.amr.games.pacman.ui.fx.shell;

import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.lib.Logging;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Maybe this is total bullshit but it works.
 * 
 * @author Armin Reichert
 */
public class Keyboard {

	public static final byte NO_MODIFIER = 0x0, ALT = 0x1, CTRL = 0x2, SHIFT = 0x4;

	private static KeyEvent currentEvent;
	private static byte currentMask;
	private static final List<Runnable> handlers = new ArrayList<>();

	public static void processEvent(KeyEvent e) {
		if (e.isConsumed()) {
			return;
		}
		currentEvent = e;
		currentMask = NO_MODIFIER;
		if (e.isAltDown()) {
			currentMask |= ALT;
		}
		if (e.isControlDown()) {
			currentMask |= CTRL;
		}
		if (e.isShiftDown()) {
			currentMask |= SHIFT;
		}
		handlers.forEach(Runnable::run);
		e.consume();
	}

	public static void addHandler(Runnable handler) {
		handlers.add(handler);
	}

	public static void removeHandler(Runnable handler) {
		handlers.remove(handler);
	}

	public static boolean pressed(KeyCode code) {
		return pressed(NO_MODIFIER, code);
	}

	public static boolean pressed(int modfierMask, KeyCode code) {
		if (currentEvent != null && currentEvent.getCode() == code && currentMask == modfierMask) {
			Logging.log("Key press handled: %s%s", modifierText(currentMask), code);
			currentEvent.consume();
			return true;
		}
		return false;
	}

	private static String modifierText(byte mask) {
		if (mask == NO_MODIFIER) {
			return "(NO MODIFIER) ";
		}
		String text = "";
		if ((mask & ALT) != 0) {
			text += "ALT";
		}
		if ((mask & CTRL) != 0) {
			text += " CONTROL";
		}
		if ((mask & SHIFT) != 0) {
			text += " SHIFT";
		}
		return (text + "+").trim();
	}
}