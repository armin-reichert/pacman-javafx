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

import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.BonusAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
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

	private final Bonus bonus;
	private final GenericAnimation<Integer> jumpAnimation;
	public final BonusAnimations animations;

	public Bonus2D(Bonus bonus, Rendering2D r2D) {
		this.bonus = bonus;
		jumpAnimation = new GenericAnimation<>(2, -2);
		jumpAnimation.frameDuration(10);
		jumpAnimation.repeatForever();
		animations = new BonusAnimations(r2D);
		animations.select(BonusAnimations.Key.ANIM_SYMBOL);
	}

	public void startJumping() {
		jumpAnimation.restart();
	}

	public void stopJumping() {
		jumpAnimation.stop();
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		if (bonus.state() != BonusState.INACTIVE) {
			var sprite = animations.currentSprite(bonus);
			if (sprite != null) {
				if (jumpAnimation.isRunning()) {
					g.save();
					g.translate(0, jumpAnimation.animate());
					r2D.drawSpriteCenteredOverBox(g, sprite, bonus.position().x, bonus.position().y);
					g.restore();
				} else {
					r2D.drawSpriteCenteredOverBox(g, sprite, bonus.position().x, bonus.position().y);
				}
			}
		}
	}
}