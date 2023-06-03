/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimationsPacManGame extends SpriteAnimations
		implements GhostAnimations<SpriteAnimation, Rectangle2D> {

	private final Ghost ghost;
	private final SpritesheetPacManGame spritesheet;

	public GhostAnimationsPacManGame(Ghost ghost, SpritesheetPacManGame spritesheet) {
		Globals.checkNotNull(ghost);
		Globals.checkNotNull(spritesheet);
		this.ghost = ghost;
		this.spritesheet = spritesheet;

		//@formatter:off
		var normal = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostNormalSprites(ghost.id(), Direction.LEFT))
				.frameTicks(8)
				.loop()
			.end();

		var frightened = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostFrightenedSprites())
				.frameTicks(8)
				.loop()
			.end();
		
		var flashing = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostFlashingSprites())
				.frameTicks(6)
				.loop()
			.end();
		
		var eyesAnimation = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostEyesSprites(Direction.LEFT))
			.end();
		
		var numberAnimation = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostNumberSprites())
			.end();
		
		var damaged = SpriteAnimation
			.begin()
				.sprites(spritesheet.blinkyDamagedSprites())
			.end();
		
		var stretched = SpriteAnimation
			.begin()
				.sprites(spritesheet.blinkyStretchedSprites())
			.end();
		
		var patched = SpriteAnimation
			.begin()
				.sprites(spritesheet.blinkyPatchedSprites())
				.frameTicks(4)
				.loop()
			.end();
		
		var naked = SpriteAnimation
			.begin()
			.sprites(spritesheet.blinkyNakedSprites())
				.frameTicks(4)
				.loop()
			.end();
		//@formatter:on

		animationsByName.put(GHOST_NORMAL, normal);
		animationsByName.put(GHOST_FRIGHTENED, frightened);
		animationsByName.put(GHOST_FLASHING, flashing);
		animationsByName.put(GHOST_EYES, eyesAnimation);
		animationsByName.put(GHOST_NUMBER, numberAnimation);
		animationsByName.put(BLINKY_DAMAGED, damaged);
		animationsByName.put(BLINKY_STRETCHED, stretched);
		animationsByName.put(BLINKY_PATCHED, patched);
		animationsByName.put(BLINKY_NAKED, naked);

		// TODO check this
		eyesAnimation.start();
		frightened.start();
		flashing.start();
	}

	@Override
	public void select(String name, Object... args) {
		super.select(name, args);
		if (GHOST_NUMBER.equals(name)) {
			byName(GHOST_NUMBER).setFrameIndex((int) args[0]);
		}
	}

	@Override
	public void updateCurrentAnimation() {
		if (GHOST_NORMAL.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.ghostEyesSprites(ghost.wishDir()));
		}
	}
}