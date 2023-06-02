/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class SpriteAnimations implements Animations<SpriteAnimation, Rectangle2D> {

	protected final Map<String, SpriteAnimation> animationsByName = new HashMap<>();
	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	@Override
	public void select(String name, Object... args) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = byName(name);
			if (currentAnimation != null) {
				currentAnimation.setFrameIndex(0);
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
	public SpriteAnimation selectedAnimation() {
		return currentAnimation;
	}

	@Override
	public String selectedAnimationName() {
		return currentAnimationName;
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

	@Override
	public final Rectangle2D currentSprite() {
		if (currentAnimationName != null) {
			updateCurrentAnimation();
			return currentAnimation.currentSprite();
		}
		return null;
	}

	protected void updateCurrentAnimation() {
		// for example to adjust to current ghost direction
	}
}