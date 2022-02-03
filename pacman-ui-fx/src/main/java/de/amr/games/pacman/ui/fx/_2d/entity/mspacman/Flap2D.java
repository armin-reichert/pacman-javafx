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
package de.amr.games.pacman.ui.fx._2d.entity.mspacman;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.entities.Flap;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * The film flap used at the beginning of the Ms. Pac-Man intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap2D {

	private final Flap flap;
	private Rendering2D_MsPacMan rendering;
	public final TimedSequence<Rectangle2D> animation;

	public Flap2D(Flap flap, Rendering2D_MsPacMan rendering) {
		this.flap = flap;
		this.rendering = rendering;
		animation = rendering.createFlapAnimation();
	}

	public void render(GraphicsContext g) {
		if (flap.visible) {
			Rectangle2D sprite = animation.animate();
			rendering.renderEntity(g, flap, sprite);
			g.setFont(rendering.getScoreFont());
			g.setFill(Color.rgb(222, 222, 255));
			g.fillText(String.valueOf(flap.sceneNumber), flap.position.x + sprite.getWidth() - 25,
					flap.position.y + 18);
			g.fillText(flap.sceneTitle, flap.position.x + sprite.getWidth(), flap.position.y);
		}
	}
}