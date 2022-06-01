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

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ISpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimationCollection;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimationMap;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D extends GameEntity2D {

	public enum GhostAnimation {
		KICKING, EYES, NUMBER, FRIGHTENED, FLASHING;
	};

	public class Animations extends SpriteAnimationCollection<GhostAnimation> {

		private SpriteAnimationMap<Direction> eyes;
		private SpriteAnimation flashing;
		private SpriteAnimation frightened;
		private SpriteAnimationMap<Direction> kicking;
		private SpriteAnimation number;

		public Animations(Rendering2D r2D) {
			eyes = r2D.createGhostReturningHomeAnimations();
			flashing = r2D.createGhostFlashingAnimation();
			frightened = r2D.createGhostFrightenedAnimation();
			kicking = r2D.createGhostKickingAnimations(ghost.id);
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
		public Stream<ISpriteAnimation> all() {
			return Stream.of(eyes, flashing, frightened, kicking, number);
		}

	}

	public final Ghost ghost;
	public final Animations animations;

	public Ghost2D(Ghost ghost, GameModel game, Rendering2D r2D) {
		super(game);
		this.ghost = ghost;
		animations = new Animations(r2D);
		animations.select(GhostAnimation.KICKING);
	}

	@Override
	public void render(GraphicsContext g, Rendering2D r2D) {
		var sprite = switch (animations.selectedKey()) {
		case EYES -> animations.eyes.get(ghost.wishDir()).animate();
		case FLASHING -> animations.flashing.animate();
		case FRIGHTENED -> animations.frightened.animate();
		case KICKING -> animations.kicking.get(ghost.wishDir()).animate();
		case NUMBER -> animations.number.frame(numberFrame(ghost.bounty));
		};
		r2D.drawEntity(g, ghost, sprite);
	}

	public void startFlashingAnimation(int numFlashes, long ticksTotal) {
		long frameTicks = ticksTotal / (numFlashes * animations.flashing.numFrames());
		animations.flashing.frameDuration(frameTicks).repetitions(numFlashes).restart();
	}

	public void refresh() {
		visible = ghost.visible;
		animations.kicking.ensureRunning();
		animations.frightened.ensureRunning();
		animations.flashing.ensureRunning();
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