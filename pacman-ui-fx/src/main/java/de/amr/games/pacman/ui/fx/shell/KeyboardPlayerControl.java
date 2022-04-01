/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import de.amr.games.pacman.controller.PlayerControl;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

/**
 * Controls player movement using keys.
 * 
 * @author Armin Reichert
 */
public class KeyboardPlayerControl implements PlayerControl {

	private final KeyCode upCode, downCode, leftCode, rightCode;
	private boolean up, down, left, right;

	public KeyboardPlayerControl(Window window) {
		this(window, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
	}

	public KeyboardPlayerControl(Window window, KeyCode upCode, KeyCode downCode, KeyCode leftCode, KeyCode rightCode) {
		this.upCode = upCode;
		this.downCode = downCode;
		this.leftCode = leftCode;
		this.rightCode = rightCode;
		window.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		window.addEventHandler(KeyEvent.KEY_RELEASED, this::onKeyReleased);
	}

	@Override
	public void steer(Pac player) {
		if (up) {
			player.setWishDir(Direction.UP);
		} else if (down) {
			player.setWishDir(Direction.DOWN);
		} else if (left) {
			player.setWishDir(Direction.LEFT);
		} else if (right) {
			player.setWishDir(Direction.RIGHT);
		}
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown() || e.isShiftDown() || e.isAltDown()) {
			return;
		}
		if (e.getCode() == upCode) {
			up = true;
		} else if (e.getCode() == downCode) {
			down = true;
		} else if (e.getCode() == leftCode) {
			left = true;
		} else if (e.getCode() == rightCode) {
			right = true;
		}
	}

	private void onKeyReleased(KeyEvent e) {
		if (e.isControlDown()) {
			return;
		}
		if (e.getCode() == upCode) {
			up = false;
		} else if (e.getCode() == downCode) {
			down = false;
		} else if (e.getCode() == leftCode) {
			left = false;
		} else if (e.getCode() == rightCode) {
			right = false;
		}
	}
}