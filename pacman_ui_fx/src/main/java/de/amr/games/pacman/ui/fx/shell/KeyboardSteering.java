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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameModel;
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
	private final KeyCode up;
	private final KeyCode down;
	private final KeyCode left;
	private final KeyCode right;

	public KeyboardSteering(KeyCode up, KeyCode down, KeyCode left, KeyCode right) {
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
	}

	public void onKeyPressed(KeyEvent e) {
		boolean noModifier = !(e.isAltDown() || e.isControlDown() || e.isShiftDown());
		boolean shiftOnly = e.isShiftDown() && !e.isAltDown() && !e.isControlDown();
		var code = e.getCode();
		dir = null;
		if (code == up && (noModifier || shiftOnly)) {
			dir = Direction.UP;
		} else if (code == down && (noModifier || shiftOnly)) {
			dir = Direction.DOWN;
		} else if (code == left && (noModifier || shiftOnly)) {
			dir = Direction.LEFT;
		} else if (code == right && (noModifier || shiftOnly)) {
			dir = Direction.RIGHT;
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void steer(GameModel game, Creature pac) {
		if (dir != null) {
			pac.setWishDir(dir);
		}
		dir = null;
	}
}