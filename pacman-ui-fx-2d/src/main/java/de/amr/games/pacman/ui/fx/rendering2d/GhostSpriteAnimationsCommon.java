/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class GhostSpriteAnimationsCommon implements GhostAnimations<SpriteAnimation, Rectangle2D> {

	protected final Map<String, SpriteAnimation> animationsByName = new HashMap<>();
	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	@Override
	public String selectedAnimationName() {
		return currentAnimationName;
	}

	@Override
	public SpriteAnimation selectedAnimation() {
		return currentAnimation;
	}

	@Override
	public void select(String name, Object... args) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = byName(name);
			if (currentAnimation != null) {
				if (currentAnimation == byName(GHOST_NUMBER)) {
					currentAnimation.setFrameIndex((Integer) args[0]);
				} else {
					currentAnimation.setFrameIndex(0);
				}
			}
		}
	}

	public SpriteAnimation byName(String name) {
		if (animationsByName.containsKey(name)) {
			return animationsByName.get(name);
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}

	@Override
	public void startSelected() {
		if (currentAnimation != null) {
			currentAnimation.start();
		}
	}

	@Override
	public void stopSelected() {
		if (currentAnimation != null) {
			currentAnimation.stop();
		}
	}

	@Override
	public void resetSelected() {
		if (currentAnimation != null) {
			currentAnimation.reset();
		}
	}
}