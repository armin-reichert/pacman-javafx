/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.model.actors.Animations;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class SpriteAnimations implements Animations {

	protected String currentAnimationName;

	public String currentAnimationName() {
		return currentAnimationName;
	}

	@Override
	public SpriteAnimation currentAnimation() {
		return byName(currentAnimationName);
	}

	public abstract SpriteAnimation byName(String name);

	@Override
	public void select(String name, Object... args) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			if (currentAnimation() != null) {
				currentAnimation().setFrameIndex(0);
			}
		}
	}

	@Override
	public void startSelected() {
		if (currentAnimation() != null) {
			currentAnimation().start();
		}
	}

	@Override
	public void stopSelected() {
		if (currentAnimation() != null) {
			currentAnimation().stop();
		}
	}

	@Override
	public void resetSelected() {
		if (currentAnimation() != null) {
			currentAnimation().reset();
		}
	}

	public final Rectangle2D currentSprite() {
		if (currentAnimation() != null) {
			updateCurrentAnimation();
			return currentAnimation().currentSprite();
		}
		return null;
	}

	protected void updateCurrentAnimation() {
		// for example to adjust to current ghost direction
	}
}