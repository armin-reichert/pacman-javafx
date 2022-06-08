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
package de.amr.games.pacman.ui.fx._2d.entity.pacman;

import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * The big Pac-Man from the first intermission scene in Pac-Man.
 * 
 * @author Armin Reichert
 */
public class BigPacMan2D {

	private final Pac pacMan;
	private final GenericAnimation<Rectangle2D> munchingAnimation;

	public BigPacMan2D(Pac pacMan) {
		this.pacMan = pacMan;
		munchingAnimation = Spritesheet_PacMan.get().createBigPacManMunchingAnimation();
	}

	public void startMunching() {
		munchingAnimation.restart();
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		Rectangle2D sprite = munchingAnimation.animate();
		// lift it up such that it sits on the ground instead of being vertically
		// centered to the ground
		g.save();
		g.translate(0, -sprite.getHeight() / 2 + 8);
		r2D.drawEntity(g, pacMan, sprite);
		g.restore();
	}
}