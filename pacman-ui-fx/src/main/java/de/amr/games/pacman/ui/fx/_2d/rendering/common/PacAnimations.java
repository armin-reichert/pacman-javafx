/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx._2d.rendering.common;

import java.util.HashMap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.model.common.actors.Pac;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacAnimations extends SpriteAnimations<Pac> {

	public static final String DYING = "dying";
	public static final String MUNCHING = "munching";

	public PacAnimations(Rendering2D r2D) {
		animationsByName = new HashMap<>(2);
		put(DYING, r2D.createPacDyingAnimation());
		put(MUNCHING, r2D.createPacMunchingAnimationMap());
		select(MUNCHING);
	}

	@Override
	public void ensureRunning() {
		byName(MUNCHING).ensureRunning();
	}

	@Override
	public Rectangle2D current(Pac pac) {
		return switch (selected) {
		case MUNCHING -> super.<Direction, Rectangle2D>castToMap(MUNCHING).get(pac.moveDir()).animate();
		default -> (Rectangle2D) selectedAnimation().animate();
		};
	}
}