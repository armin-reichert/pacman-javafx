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

import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.Objects;

import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D-representation of the bonus symbol.
 * <p>
 * In Ms. Pac-Man, the bonus is jumping up and down while wandering the maze.
 * <p>
 * TODO: Animation is not 100% accurate
 * 
 * @author Armin Reichert
 */
public class Bonus2D {

	private final GameModel game;
	private final Rendering2D r2D;
	private final TimedSeq<Integer> animation;

	public Bonus2D(GameModel game, Rendering2D r2D, boolean moving) {
		this.game = game;
		this.r2D = Objects.requireNonNull(r2D);
		animation = moving ? TimedSeq.of(2, 0, -2).frameDuration(8).endless() : null;
	}

	public void startAnimation() {
		if (animation != null) {
			animation.restart();
		}
	}

	public void stopAnimation() {
		if (animation != null) {
			animation.stop();
		}
	}

	public void render(GraphicsContext g) {
		Rectangle2D sprite = switch (game.bonusState) {
		case EDIBLE -> r2D.getSymbolSprite(game.level.bonusSymbol);
		case EATEN -> r2D.getBonusValueSprite(game.bonusValue(game.level.bonusSymbol));
		default -> null;
		};
		if (sprite != null) {
			double sw = sprite.getWidth(), sh = sprite.getHeight();
			int ty = animation != null ? animation.animate() : 0;
			g.save();
			g.translate(0, ty);
			r2D.renderSprite(g, sprite, game.bonusPosition().x + HTS - sw / 2, game.bonusPosition().y + HTS - sh / 2);
			g.restore();
		}
	}
}