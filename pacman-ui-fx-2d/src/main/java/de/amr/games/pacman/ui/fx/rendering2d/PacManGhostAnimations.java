/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGhostAnimations extends SpriteAnimations {

	private final Ghost ghost;
	private final PacManSpriteSheet spriteSheet;

	public PacManGhostAnimations(Ghost ghost, PacManSpriteSheet spriteSheet) {
		checkNotNull(ghost);
		checkNotNull(spriteSheet);
		this.ghost = ghost;
		this.spriteSheet = spriteSheet;

		var normal = SpriteAnimation
			.begin()
				.sprites(spriteSheet.ghostNormalSprites(ghost.id(), Direction.LEFT))
				.frameTicks(8)
				.loop()
			.end();

		var frightened = SpriteAnimation
			.begin()
				.sprites(spriteSheet.ghostFrightenedSprites())
				.frameTicks(8)
				.loop()
			.end();
		
		var flashing = SpriteAnimation
			.begin()
				.sprites(spriteSheet.ghostFlashingSprites())
				.frameTicks(6)
				.loop()
			.end();
		
		var eyesAnimation = SpriteAnimation
			.begin()
				.sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
			.end();
		
		var numberAnimation = SpriteAnimation
			.begin()
				.sprites(spriteSheet.ghostNumberSprites())
			.end();
		
		var damaged = SpriteAnimation
			.begin()
				.sprites(spriteSheet.blinkyDamagedSprites())
			.end();
		
		var stretched = SpriteAnimation
			.begin()
				.sprites(spriteSheet.blinkyStretchedSprites())
			.end();
		
		var patched = SpriteAnimation
			.begin()
				.sprites(spriteSheet.blinkyPatchedSprites())
				.frameTicks(4)
				.loop()
			.end();
		
		var naked = SpriteAnimation
			.begin()
				.sprites(spriteSheet.blinkyNakedSprites())
				.frameTicks(4)
				.loop()
			.end();

		animationsByName.put(GhostAnimations.GHOST_NORMAL,     normal);
		animationsByName.put(GhostAnimations.GHOST_FRIGHTENED, frightened);
		animationsByName.put(GhostAnimations.GHOST_FLASHING,   flashing);
		animationsByName.put(GhostAnimations.GHOST_EYES,       eyesAnimation);
		animationsByName.put(GhostAnimations.GHOST_NUMBER,     numberAnimation);
		animationsByName.put(GhostAnimations.BLINKY_DAMAGED,   damaged);
		animationsByName.put(GhostAnimations.BLINKY_STRETCHED, stretched);
		animationsByName.put(GhostAnimations.BLINKY_PATCHED,   patched);
		animationsByName.put(GhostAnimations.BLINKY_NAKED,     naked);

		// TODO check this
		eyesAnimation.start();
		frightened.start();
		flashing.start();
	}

	@Override
	public void select(String name, Object... args) {
		super.select(name, args);
		if (GhostAnimations.GHOST_NUMBER.equals(name)) {
			byName(GhostAnimations.GHOST_NUMBER).setFrameIndex((int) args[0]);
		}
	}

	@Override
	public void updateCurrentAnimation() {
		if (GhostAnimations.GHOST_NORMAL.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (GhostAnimations.GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.ghostEyesSprites(ghost.wishDir()));
		}
	}
}