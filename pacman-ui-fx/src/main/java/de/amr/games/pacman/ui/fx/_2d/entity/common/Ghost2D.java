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

	public final Ghost ghost;
	public GhostAnimation selectedAnimationKey;

	private SpriteAnimationMap<Direction> animEyes;
	private SpriteAnimation animFlashing;
	private SpriteAnimation animFrightened;
	public SpriteAnimationMap<Direction> animKicking;
	public SpriteAnimation animNumber;

	public Ghost2D(Ghost ghost, GameModel game, Rendering2D r2D) {
		super(game);
		this.ghost = ghost;
		animEyes = r2D.createGhostReturningHomeAnimations();
		animFlashing = r2D.createGhostFlashingAnimation();
		animFrightened = r2D.createGhostFrightenedAnimation();
		animKicking = r2D.createGhostKickingAnimations(ghost.id);
		animNumber = SpriteAnimation.of(r2D.getBountyNumberSprite(200), r2D.getBountyNumberSprite(400),
				r2D.getBountyNumberSprite(800), r2D.getBountyNumberSprite(1600));

		selectAnimation(GhostAnimation.KICKING);
	}

	@Override
	public void render(GraphicsContext g, Rendering2D r2D) {
		var sprite = switch (selectedAnimationKey) {
		case EYES -> animEyes.get(ghost.wishDir()).animate();
		case FLASHING -> animFlashing.animate();
		case FRIGHTENED -> animFrightened.animate();
		case KICKING -> animKicking.get(ghost.wishDir()).animate();
		case NUMBER -> animNumber.frame(numberFrame(ghost.bounty));
		};
		r2D.drawEntity(g, ghost, sprite);
	}

	public void selectAnimation(GhostAnimation key) {
		this.selectedAnimationKey = key;
		animation(selectedAnimationKey).ensureRunning();
	}

	public void stop(GhostAnimation key) {
		animation(key).stop();
	}

	public void run(GhostAnimation key) {
		animation(key).run();
	}

	public void resetAnimations() {
		Stream.of(animEyes, animFlashing, animFrightened, animKicking, animNumber).forEach(ISpriteAnimation::reset);
	}

	public void stopAnimations() {
		Stream.of(animEyes, animFlashing, animFrightened, animKicking, animNumber).forEach(ISpriteAnimation::stop);
	}

	public void runAnimations() {
		Stream.of(animEyes, animFlashing, animFrightened, animKicking, animNumber).forEach(ISpriteAnimation::run);
	}

	public void restartAnimations() {
		Stream.of(animEyes, animFlashing, animFrightened, animKicking, animNumber).forEach(ISpriteAnimation::restart);
	}

	public void startFlashingAnimation(int numFlashes, long ticksTotal) {
		animFrightened.stop();
		long frameTicks = ticksTotal / (numFlashes * animFlashing.numFrames());
		animFlashing.frameDuration(frameTicks).repetitions(numFlashes).restart();
	}

	public void refresh() {
		visible = ghost.visible;
		animKicking.ensureRunning();
		animFrightened.ensureRunning();
		animFlashing.ensureRunning();
	}

	private ISpriteAnimation animation(GhostAnimation key) {
		return switch (key) {
		case EYES -> animEyes;
		case FLASHING -> animFlashing;
		case FRIGHTENED -> animFrightened;
		case KICKING -> animKicking;
		case NUMBER -> animNumber;
		};
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