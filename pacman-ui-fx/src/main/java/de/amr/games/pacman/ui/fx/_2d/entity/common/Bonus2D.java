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

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.BonusState;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D-representation of the bonus in Pac-Man and Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Bonus2D  {

	private final Rendering2D rendering;
	private final Bonus bonus;
	public final Optional<TimedSequence<Integer>> animation; // Ms. Pac-Man only

	public Bonus2D(Bonus bonus, Rendering2D r2D, boolean animated) {
		this.bonus = Objects.requireNonNull(bonus);
		this.rendering = Objects.requireNonNull(r2D);
		if (animated) {
			animation = Optional.of(TimedSequence.of(2, 0, -2).frameDuration(8).endless());
		} else {
			animation = Optional.empty();
		}
	}

	public void render(GraphicsContext g) {
		Rectangle2D sprite;
		if (bonus.state == BonusState.EDIBLE) {
			sprite = rendering.getSymbolSprites().get(bonus.symbol);
		} else if (bonus.state == BonusState.EATEN) {
			sprite = rendering.getBonusValueSprites().get(bonus.points);
		} else {
			return;
		}
		if (animation.isPresent()) {
			// Ms. Pac.Man bonus is jumping up and down while wandering the maze
			g.save();
			g.translate(0, animation.get().animate());
			rendering.renderEntity(g, bonus, sprite);
			g.restore();
		} else {
			rendering.renderEntity(g, bonus, sprite);
		}
	}
}