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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.animation.SpriteAnimation;
import de.amr.games.pacman.model.common.LevelCounter;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Displays the symbols of the last (at most 7) levels.
 * 
 * @author Armin Reichert
 */
public class LevelCounter2D {

	private final LevelCounter levelCounter;
	private final SpriteAnimation<Rectangle2D> symbolAnimation;
	public boolean visible;
	public double right_x, y;
	private double x;

	/**
	 * @param game    the game
	 * @param right_x the RIGHT border x-coordinate of the counter!
	 * @param y       the y-coordinate
	 */
	public LevelCounter2D(LevelCounter levelCounter, Rendering2D r2D) {
		this.levelCounter = levelCounter;
		this.right_x = t(24);
		this.y = t(34);
		this.symbolAnimation = r2D.createBonusSymbolAnimation();
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		if (visible) {
			x = right_x;
			levelCounter.symbols().forEach(symbol -> {
				var sprite = symbolAnimation.frame(symbol);
				r2D.drawSprite(g, sprite, x, y);
				x -= t(2);
			});
		}
	}
}