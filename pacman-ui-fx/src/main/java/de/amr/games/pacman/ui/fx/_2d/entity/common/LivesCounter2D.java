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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;

/**
 * 2D lives counter.
 * 
 * @author Armin Reichert
 */
public class LivesCounter2D extends GameEntity2D {

	public int maxLives = 5;
	private final Rendering2D r2D;

	public LivesCounter2D(GameModel game, Rendering2D r2D) {
		super(game);
		this.r2D = r2D;
	}

	@Override
	public void render(GraphicsContext g) {
		if (!visible) {
			return;
		}
		var sprite = r2D.getLifeSprite();
		for (int i = 0; i < Math.min(game.lives, maxLives); ++i) {
			r2D.renderSprite(g, sprite, x + t(2 * i), y);
		}
		// show text if more lives are available than displayed
		if (game.lives > maxLives) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Serif", FontWeight.BOLD, 8));
			g.setFontSmoothingType(FontSmoothingType.LCD);
			g.fillText("+" + (game.lives - maxLives), x + t(10), y + t(1));
		}
	}
}