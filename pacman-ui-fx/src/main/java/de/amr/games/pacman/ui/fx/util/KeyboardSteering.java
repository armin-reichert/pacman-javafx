/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controls Pac-Man using specified keys.
 * 
 * @author Armin Reichert
 */
public class KeyboardSteering implements Steering {

	private Direction dir;
	public KeyCode up;
	public KeyCode down;
	public KeyCode left;
	public KeyCode right;

	public KeyboardSteering(KeyCode up, KeyCode down, KeyCode left, KeyCode right) {
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
	}

	public void onKeyPressed(KeyEvent e) {
		var code = e.getCode();
		boolean noModifier = !(e.isAltDown() || e.isControlDown() || e.isShiftDown());
		boolean shiftOnly = e.isShiftDown() && !e.isAltDown() && !e.isControlDown();
		dir = null;
		if (noModifier || shiftOnly) {
			if (code == up) {
				dir = Direction.UP;
				e.consume();
			} else if (code == down) {
				dir = Direction.DOWN;
				e.consume();
			} else if (code == left) {
				dir = Direction.LEFT;
				e.consume();
			} else if (code == right) {
				dir = Direction.RIGHT;
				e.consume();
			}
		}
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		if (dir != null) {
			guy.setWishDir(dir);
			dir = null;
		}
	}
}