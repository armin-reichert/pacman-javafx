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

package de.amr.games.pacman.ui.fx._2d.entity.common;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D.GhostAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.AnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ISpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimationMap;
import javafx.geometry.Rectangle2D;

public class GhostAnimations extends AnimationSet<GhostAnimation> {

	private SpriteAnimationMap<Direction> eyes;
	private SpriteAnimation flashing;
	private SpriteAnimation frightened;
	private SpriteAnimationMap<Direction> kicking;
	private SpriteAnimation number;

	public GhostAnimations(int ghostID, Rendering2D r2D) {
		eyes = r2D.createGhostReturningHomeAnimations();
		flashing = r2D.createGhostFlashingAnimation();
		frightened = r2D.createGhostFrightenedAnimation();
		kicking = r2D.createGhostKickingAnimations(ghostID);
		number = SpriteAnimation.of(r2D.getBountyNumberSprite(200), r2D.getBountyNumberSprite(400),
				r2D.getBountyNumberSprite(800), r2D.getBountyNumberSprite(1600));
	}

	@Override
	public ISpriteAnimation animation(GhostAnimation key) {
		return switch (key) {
		case EYES -> eyes;
		case FLASHING -> flashing;
		case FRIGHTENED -> frightened;
		case KICKING -> kicking;
		case NUMBER -> number;
		};
	}

	@Override
	public Stream<ISpriteAnimation> animations() {
		return Stream.of(eyes, flashing, frightened, kicking, number);
	}

	public Rectangle2D currentSprite(Ghost ghost) {
		return switch (selectedKey()) {
		case EYES -> eyes.get(ghost.wishDir()).animate();
		case FLASHING -> flashing.animate();
		case FRIGHTENED -> frightened.animate();
		case KICKING -> {
			var sprite = kicking.get(ghost.wishDir()).frame();
			if (ghost.velocity.length() > 0) {
				kicking.get(ghost.wishDir()).advance();
			}
			yield sprite;
		}
		case NUMBER -> number.frame(numberFrame(ghost.bounty));
		};
	}

	public void startFlashing(int numFlashes, long ticksTotal) {
		long frameTicks = ticksTotal / (numFlashes * flashing.numFrames());
		flashing.frameDuration(frameTicks).repetitions(numFlashes).restart();
	}

	public void refresh() {
		kicking.ensureRunning();
		frightened.ensureRunning();
		flashing.ensureRunning();
	}

	private int numberFrame(int number) {
		return switch (number) {
		case 200 -> 0;
		case 400 -> 1;
		case 800 -> 2;
		case 1600 -> 3;
		default -> throw new IllegalArgumentException();
		};
	}
}