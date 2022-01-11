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

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.BonusState;
import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D-representation of the bonus in Pac-Man and Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Bonus2D implements Renderable2D {

	private final Rendering2D rendering;
	private final Bonus bonus;
	private final TimedSequence<Integer> jumpingAnimation; // Ms. Pac-Man only

	public Bonus2D(Bonus bonus, Rendering2D rendering) {
		this.bonus = Objects.requireNonNull(bonus);
		this.rendering = Objects.requireNonNull(rendering);
		if (rendering instanceof Rendering2D_MsPacMan) {
			Rendering2D_MsPacMan msPacManRendering = (Rendering2D_MsPacMan) rendering;
			jumpingAnimation = msPacManRendering.createBonusAnimation();
		} else {
			jumpingAnimation = null;
		}
	}

	public void startAnimation() {
		if (jumpingAnimation != null) {
			jumpingAnimation.restart();
		}
	}

	public void stopAnimation() {
		if (jumpingAnimation != null) {
			jumpingAnimation.stop();
		}
	}

	@Override
	public void render(GraphicsContext g) {
		if (bonus.visible) {
			Rectangle2D sprite = currentSprite();
			// Ms. Pac.Man bonus is jumping up and down while wandering the maze
			int dy = jumpingAnimation != null ? jumpingAnimation.animate() : 0;
			g.save();
			g.translate(0, dy);
			rendering.renderEntity(g, bonus, sprite);
			g.restore();
		}
	}

	private Rectangle2D currentSprite() {
		if (bonus.state == BonusState.EDIBLE) {
			return rendering.getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.state == BonusState.EATEN) {
			return rendering.getBonusValuesSprites().get(bonus.points);
		}
		return null;
	}
}