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

import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * 2D representation of the world. Implements the flashing animation played on the end of each level.
 * 
 * @author Armin Reichert
 */
public class World2D {

	private final SingleGenericAnimation<Image> flashingAnimation;

	public World2D(Rendering2D r2D, int mazeNumber) {
		this.flashingAnimation = r2D.createMazeFlashingAnimation(mazeNumber);
	}

	public void startFlashing(int numFlashes) {
		flashingAnimation.repeat(numFlashes);
		flashingAnimation.restart();
	}

	public void render(GraphicsContext g, Rendering2D r2D, World world, int mazeNumber, boolean foodHidden) {
		if (flashingAnimation.isRunning()) {
			g.drawImage(flashingAnimation.animate(), 0, t(3));
		} else {
			r2D.drawWorld(g, world, mazeNumber, foodHidden);
		}
	}
}