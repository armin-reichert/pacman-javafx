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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the player (Pac-Man or Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class Pac2D extends GameEntity2D {

	public final Pac pac;
	public final PacAnimationSet animations;

	public Pac2D(Pac pac, GameModel game, PacAnimationSet animations) {
		super(game);
		this.pac = pac;
		this.animations = animations;
		animations.selectAnimation(PacAnimation.MUNCHING);
	}

	@Override
	public void render(GraphicsContext g, Rendering2D r2D) {
		r2D.drawEntity(g, pac, animations.currentSprite(pac));
	}
}