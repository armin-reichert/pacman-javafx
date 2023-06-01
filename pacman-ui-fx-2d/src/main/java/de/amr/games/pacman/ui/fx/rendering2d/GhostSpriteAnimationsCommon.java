/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.function.Consumer;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class GhostSpriteAnimationsCommon implements GhostAnimations<SpriteAnimation> {

	protected final Ghost ghost;
	protected Spritesheet spritesheet;

	protected SpriteAnimation eyesAnimation;
	protected SpriteAnimation normalAnimation;
	protected SpriteAnimation frightenedAnimation;
	protected SpriteAnimation flashingAnimation;
	protected SpriteAnimation numberAnimation;

	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	protected GhostSpriteAnimationsCommon(Ghost ghost, Spritesheet sprites) {
		this.ghost = ghost;
		this.spritesheet = sprites;

		normalAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(8) //
				.loop() //
				.sprites(ghostNormalSprites(ghost.id(), Direction.RIGHT)) //
				.build();

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

		eyesAnimation = new SpriteAnimation.Builder().sprites(ghostEyesSprites(Direction.LEFT)).build();

		numberAnimation = new SpriteAnimation.Builder() //
				.sprites(ghostNumberSprites()) //
				.build();

		// TODO check this
		eyesAnimation.start();
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

	private void withCurrentAnimationDo(Consumer<SpriteAnimation> operation) {
		if (currentAnimation != null) {
			operation.accept(currentAnimation);
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
		if (currentAnimation == normalAnimation) {
			normalAnimation.setSprites(ghostNormalSprites(ghost.id(), ghost.wishDir()));
		}
		if (currentAnimation == eyesAnimation) {
			eyesAnimation.setSprites(ghostEyesSprites(ghost.wishDir()));
		}
		return currentAnimation.frame();
	}

	protected SpriteAnimation animationByName(String name) {
		if (GHOST_NORMAL.equals(name)) {
			return normalAnimation;
		}
		if (GHOST_FRIGHTENED.equals(name)) {
			return frightenedAnimation;
		}
		if (GHOST_FLASHING.equals(name)) {
			return flashingAnimation;
		}
		if (GHOST_EYES.equals(name)) {
			return eyesAnimation;
		}
		if (GHOST_NUMBER.equals(name)) {
			return numberAnimation;
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}
}