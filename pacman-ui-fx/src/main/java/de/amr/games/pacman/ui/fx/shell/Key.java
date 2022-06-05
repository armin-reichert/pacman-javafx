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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author Armin Reichert
 */
public class Key {

	public static final byte NO_MODIFIER = 0x0;
	public static final byte ALT = 0x1;
	public static final byte CTRL = 0x2;
	public static final byte SHIFT = 0x4;

	public static boolean pressed(KeyEvent e, KeyCode code) {
		return pressed(e, NO_MODIFIER, code);
	}

	public static boolean pressed(KeyEvent e, int modfierMask, KeyCode code) {
		if ((modfierMask & ALT) != 0 && !e.isAltDown()) {
			return false;
		}
		if ((modfierMask & CTRL) != 0 && !e.isControlDown()) {
			return false;
		}
		if ((modfierMask & SHIFT) != 0 && !e.isShiftDown()) {
			return false;
		}
		if (e.getCode() != code) {
			return false;
		}
		e.consume();
		return true;
	}
}