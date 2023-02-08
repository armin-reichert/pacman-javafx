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
package de.amr.games.pacman.ui.fx.input;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

/**
 * Controls Pac-Man using specified keys.
 * 
 * @author Armin Reichert
 */
public class KeyboardSteering implements Steering {

	private Direction dir;
	private KeyCodeCombination up;
	private KeyCodeCombination down;
	private KeyCodeCombination left;
	private KeyCodeCombination right;

	public KeyboardSteering(KeyCode up, KeyCode down, KeyCode left, KeyCode right) {
		this.up = new KeyCodeCombination(up);
		this.down = new KeyCodeCombination(down);
		this.left = new KeyCodeCombination(left);
		this.right = new KeyCodeCombination(right);
	}

	public void onKeyPressed(KeyEvent e) {
		dir = null;
		if (up.match(e)) {
			dir = Direction.UP;
		} else if (down.match(e)) {
			dir = Direction.DOWN;
		} else if (left.match(e)) {
			dir = Direction.LEFT;
		} else if (right.match(e)) {
			dir = Direction.RIGHT;
		}
		if (dir != null) {
			e.consume();
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