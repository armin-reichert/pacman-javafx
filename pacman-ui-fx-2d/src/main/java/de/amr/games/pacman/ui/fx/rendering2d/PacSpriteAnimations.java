/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class PacSpriteAnimations implements PacAnimations {

	protected static final Rectangle2D[] NO_SPRITES = new Rectangle2D[0];

	protected final Pac pac;
	protected Spritesheet spritesheet;

	protected Map<Direction, SpriteAnimation> munchingMap;
	protected SpriteAnimation dyingAnimation;

	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	protected PacSpriteAnimations(Pac pac, Spritesheet gss) {
		this.pac = pac;
		this.spritesheet = gss;
		createMunchingAnimation();
		createDyingAnimation();
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	protected void createMunchingAnimation() {
		munchingMap = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation.Builder() //
					.loop() //
					.sprites(pacMunchingSprites(dir)) //
					.build();
			munchingMap.put(dir, animation);
		}
	}

	protected Rectangle2D[] pacMunchingSprites(Direction dir) {
		return NO_SPRITES;
	}

	protected void createDyingAnimation() {
		dyingAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(8) //
				.sprites(pacDyingSprites()) //
				.build();
	}

	protected Rectangle2D[] pacDyingSprites() {
		return NO_SPRITES;
	}

	@Override
	public void select(String name) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = animation(name, pac.moveDir());
			if (currentAnimation != null) {
				currentAnimation.setFrame(0);
			}
		}
	}

	protected Optional<Map<Direction, SpriteAnimation>> checkIfAnimationMap(String name) {
		if (PacAnimations.PAC_MUNCHING.equals(name)) {
			return Optional.of(munchingMap);
		}
		return Optional.empty();
	}

	protected void withCurrentAnimationDo(Consumer<SpriteAnimation> operation) {
		if (currentAnimation != null) {
			var map = checkIfAnimationMap(currentAnimationName);
			if (map.isPresent()) {
				map.get().values().forEach(operation::accept);
			} else {
				operation.accept(currentAnimation);
			}
		}
	}

	@Override
	public void startSelected() {
		withCurrentAnimationDo(SpriteAnimation::start);
	}

	@Override
	public void stopSelected() {
		withCurrentAnimationDo(SpriteAnimation::stop);
	}

	@Override
	public void resetSelected() {
		withCurrentAnimationDo(SpriteAnimation::reset);
	}

	public Rectangle2D currentSprite() {
		if (!pac.isVisible() || currentAnimationName == null) {
			return null;
		}
		var map = checkIfAnimationMap(currentAnimationName);
		if (map.isPresent()) {
			currentAnimation = map.get().get(pac.moveDir());
		}
		return currentAnimation.frame();
	}

	protected SpriteAnimation animation(String name, Direction dir) {
		if (PAC_MUNCHING.equals(name)) {
			return munchingMap.get(dir);
		}
		if (PAC_DYING.equals(name)) {
			return dyingAnimation;
		}
		throw new IllegalArgumentException("Illegal animation (name, dir) value: " + name + "," + dir);
	}
}