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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Indicates current level by bonus symbols.
 * 
 * @author Armin Reichert
 */
public class LevelCounter2D extends GameEntity2D {

	public V2i rightPosition = V2i.NULL;
	private final Rendering2D r2D;

	public LevelCounter2D(GameModel game, Rendering2D r2d) {
		super(game);
		this.r2D = r2d;
	}

	@Override
	public void render(GraphicsContext g) {
		if (!visible) {
			return;
		}
		int firstLevelNumber = Math.max(1, game.levelNumber - 6);
		double x = rightPosition.x;
		for (int levelNumber = firstLevelNumber; levelNumber <= game.levelNumber; ++levelNumber, x -= t(2)) {
			r2D.renderSprite(g, r2D.getSymbolSprite(game.levelCounter.get(levelNumber - 1)), x, rightPosition.y);
		}
	}
}