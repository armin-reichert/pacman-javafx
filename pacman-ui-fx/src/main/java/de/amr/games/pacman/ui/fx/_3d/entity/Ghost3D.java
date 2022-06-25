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
package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;

/**
 * 3D representation of a ghost. A ghost is displayed in one of 4 modes:
 * <ul>
 * <li>complete ghost with colored skin and eyes,
 * <li>complete ghost with blue skin, maybe flashing, and eyes,
 * <li>eyes only,
 * <li>number cube indicating the value of the dead ghost.
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

	public enum AnimationMode {
		COLORED, FRIGHTENED, EYES, NUMBER;
	}

	public final Ghost ghost;
	private final Motion motion = new Motion();
	private final GhostValueAnimation3D numberAnimation;
	private final GhostBodyAnimation3D bodyAnimation;
	private AnimationMode animationMode;

	public Ghost3D(World world, Ghost ghost, Model3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		numberAnimation = new GhostValueAnimation3D(r2D);
		bodyAnimation = new GhostBodyAnimation3D(model3D, world, ghost);
		setAnimationMode(AnimationMode.COLORED);
	}

	public void reset(GameModel game) {
		setAnimationMode(AnimationMode.COLORED);
		update(game);
	}

	public void update(GameModel game) {
		if (ghost.killIndex != -1) {
			setAnimationMode(AnimationMode.NUMBER);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			setAnimationMode(AnimationMode.EYES);
		} else if (game.powerTimer.isRunning() && !ghost.is(GhostState.LEAVING_HOUSE)) {
			setAnimationMode(AnimationMode.FRIGHTENED);
		} else {
			setAnimationMode(AnimationMode.COLORED);
		}
		motion.update(ghost, this);
		if (animationMode == AnimationMode.COLORED
				|| animationMode == AnimationMode.FRIGHTENED && !bodyAnimation.isFlashing()) {
			bodyAnimation.update();
		}
	}

	public AnimationMode getAnimationMode() {
		return animationMode;
	}

	public void setAnimationMode(AnimationMode animationMode) {
		if (this.animationMode != animationMode) {
			this.animationMode = animationMode;
			switch (animationMode) {
			case COLORED -> {
				bodyAnimation.setShowBody(true);
				bodyAnimation.setFrightened(false);
				getChildren().setAll(bodyAnimation.getRoot());
			}
			case FRIGHTENED -> {
				bodyAnimation.setShowBody(true);
				bodyAnimation.setFrightened(true);
				getChildren().setAll(bodyAnimation.getRoot());
			}
			case EYES -> {
				bodyAnimation.setShowBody(false);
				bodyAnimation.setFrightened(false);
				getChildren().setAll(bodyAnimation.getRoot());
			}
			case NUMBER -> {
				numberAnimation.setNumber(ghost.killIndex);
				// rotate node such that number can be read from left to right
				setRotationAxis(Rotate.X_AXIS);
				setRotate(0);
				getChildren().setAll(numberAnimation.getRoot());
			}
			}
		}
	}

	public void playFlashingAnimation() {
		bodyAnimation.playFlashingAnimation();
	}
}