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

import java.util.function.Consumer;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.Pac;
import javafx.scene.input.KeyCode;

/**
 * Controls Pac-Man using keys.
 * 
 * @author Armin Reichert
 */
public class PacController implements Consumer<Pac> {

	private Direction dir;
	private final KeyCode codeUp, codeDown, codeLeft, codeRight;

	public PacController(KeyCode up, KeyCode down, KeyCode left, KeyCode right) {
		this.codeUp = up;
		this.codeDown = down;
		this.codeLeft = left;
		this.codeRight = right;
	}

	public void onKeyPressed() {
		dir = null;
		if (Key.pressed(codeUp)) {
			dir = Direction.UP;
		} else if (Key.pressed(codeDown)) {
			dir = Direction.DOWN;
		} else if (Key.pressed(codeLeft)) {
			dir = Direction.LEFT;
		} else if (Key.pressed(codeRight)) {
			dir = Direction.RIGHT;
		}
	}

	@Override
	public void accept(Pac pac) {
		if (dir != null) {
			pac.setWishDir(dir);
		}
		dir = null;
	}
}