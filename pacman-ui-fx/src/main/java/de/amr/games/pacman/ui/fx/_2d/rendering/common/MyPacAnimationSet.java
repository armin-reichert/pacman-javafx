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

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.ISpriteAnimation;
import de.amr.games.pacman.lib.SpriteAnimation;
import de.amr.games.pacman.lib.SpriteAnimationMap;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.actors.PacAnimation;
import de.amr.games.pacman.model.common.actors.PacAnimationSet;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class MyPacAnimationSet extends PacAnimationSet<Rectangle2D> {

	protected SpriteAnimationMap<Direction, Rectangle2D> munching;
	protected SpriteAnimation<Rectangle2D> dying;

	public MyPacAnimationSet(Rendering2D r2D) {
		munching = r2D.createPacMunchingAnimations();
		dying = r2D.createPacDyingAnimation();
	}

	@Override
	public ISpriteAnimation animation(PacAnimation key) {
		return switch (key) {
		case DYING -> dying;
		case MUNCHING -> munching;
		};
	}

	@Override
	public Stream<ISpriteAnimation> animations() {
		return Stream.of(munching, dying);
	}

	@Override
	public void refresh() {
		munching.ensureRunning();
		dying.reset();// TODO check this
	}

	@Override
	public Rectangle2D currentSprite(Pac pac) {
		return switch (selectedKey()) {
		case DYING -> dying.animate();
		case MUNCHING -> {
			var munchingToDir = munching.get(pac.moveDir());
			if (!pac.stuck && pac.velocity.length() > 0) {
				munchingToDir.advance();
			}
			yield munchingToDir.frame();
		}
		};
	}
}