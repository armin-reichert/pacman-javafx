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

package de.amr.games.pacman.ui.fx.scene;

import java.util.Objects;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.SceneControllerContext;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;

/**
 * @author Armin Reichert
 */
public class GameSceneContext extends SceneControllerContext {

	private final Rendering2D r2D;

	public GameSceneContext(GameController gameController, Rendering2D r2D) {
		super(gameController);
		this.r2D = Objects.requireNonNull(r2D, "2D renderer must not be NULL");
	}

	public Rendering2D r2D() {
		return r2D;
	}
}