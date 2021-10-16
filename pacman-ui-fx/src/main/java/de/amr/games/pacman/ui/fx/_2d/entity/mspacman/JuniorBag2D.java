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

import de.amr.games.pacman.model.mspacman.entities.JuniorBag;
import de.amr.games.pacman.ui.fx._2d.entity.common.Renderable2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import javafx.scene.canvas.GraphicsContext;

/**
 * The bag containing junior Pac-Man that is dropped by the stork in Ms. Pac-Man
 * intermission scene 3.
 * 
 * @author Armin Reichert
 */
public class JuniorBag2D implements Renderable2D {

	private final JuniorBag bag;
	private final Rendering2D_MsPacMan rendering;

	public JuniorBag2D(JuniorBag bag, Rendering2D_MsPacMan rendering) {
		this.bag = bag;
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext gc) {
		rendering.renderEntity(gc, bag, bag.open ? rendering.getJunior() : rendering.getBlueBag());
	}
}