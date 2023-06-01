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
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class GhostSpriteAnimations implements GhostAnimations<SpriteAnimation> {

	protected final Ghost ghost;
	protected Spritesheet spritesheet;

	protected Map<Direction, SpriteAnimation> eyesAnimationByDir;
	protected Map<Direction, SpriteAnimation> normalAnimationByDir;
	protected SpriteAnimation frightenedAnimation;
	protected SpriteAnimation flashingAnimation;
	protected SpriteAnimation numberAnimation;

	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	protected GhostSpriteAnimations(Ghost ghost, Spritesheet sprites) {
		this.ghost = ghost;
		this.spritesheet = sprites;

		normalAnimationByDir = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation.Builder() //
					.frameDurationTicks(8) //
					.loop() //
					.sprites(ghostNormalSprites(ghost.id(), dir)) //
					.build();
			normalAnimationByDir.put(dir, animation);
		}

		frightenedAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(8) //
				.loop() //
				.sprites(ghostFrightenedSprites()) //
				.build();

		flashingAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(6) //
				.loop() //
				.sprites(ghostFlashingSprites()) //
				.build();

		eyesAnimationByDir = new EnumMap<>(Direction.class);
		for (var dir : Direction.values()) {
			var animation = new SpriteAnimation.Builder().sprites(ghostEyesSprites(dir)).build();
			eyesAnimationByDir.put(dir, animation);
		}

		numberAnimation = new SpriteAnimation.Builder() //
				.sprites(ghostNumberSprites()) //
				.build();

		// TODO check this
		for (var dir : Direction.values()) {
			normalAnimationByDir.get(dir).start();
			eyesAnimationByDir.get(dir).start();
		}
		frightenedAnimation.start();
		flashingAnimation.start();
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	protected abstract Rectangle2D[] ghostNormalSprites(byte id, Direction dir);

	protected abstract Rectangle2D[] ghostFrightenedSprites();

	protected abstract Rectangle2D[] ghostFlashingSprites();

	protected abstract Rectangle2D[] ghostEyesSprites(Direction dir);

	protected abstract Rectangle2D[] ghostNumberSprites();

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
			currentAnimation = animationByName(name);
			if (currentAnimation != null) {
				if (currentAnimation == numberAnimation) {
					numberAnimation.setFrame((Integer) args[0]);
				} else {
					currentAnimation.setFrame(0);
				}
			}
		}
	}

	protected Optional<Map<Direction, SpriteAnimation>> checkIfAnimationMap(String name) {
		if (GhostAnimations.GHOST_EYES.equals(name)) {
			return Optional.of(eyesAnimationByDir);
		}
		if (GhostAnimations.GHOST_NORMAL.equals(name)) {
			return Optional.of(normalAnimationByDir);
		}
		return Optional.empty();
	}

	private void withCurrentAnimationDo(Consumer<SpriteAnimation> operation) {
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
		if (!ghost.isVisible() || currentAnimationName == null) {
			return null;
		}
		var map = checkIfAnimationMap(currentAnimationName);
		if (map.isPresent()) {
			currentAnimation = map.get().get(ghost.wishDir());
		}
		return currentAnimation.frame();
	}

	protected SpriteAnimation animationByName(String name) {
		if (GHOST_NORMAL.equals(name)) {
			return normalAnimationByDir.get(ghost.wishDir());
		}
		if (GHOST_FRIGHTENED.equals(name)) {
			return frightenedAnimation;
		}
		if (GHOST_FLASHING.equals(name)) {
			return flashingAnimation;
		}
		if (GHOST_EYES.equals(name)) {
			return eyesAnimationByDir.get(ghost.wishDir());
		}
		if (GHOST_NUMBER.equals(name)) {
			return numberAnimation;
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}
}