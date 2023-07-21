/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.mspacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimationsMsPacManGame extends SpriteAnimations
		implements Animations<SpriteAnimation, Rectangle2D> {

	private final Ghost ghost;
	private final SpritesheetMsPacManGame spritesheet;

	public GhostAnimationsMsPacManGame(Ghost ghost, SpritesheetMsPacManGame spritesheet) {
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
		
		var eyes = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostEyesSprites(Direction.LEFT))
			.end();
		
		var number = SpriteAnimation
			.begin()
				.sprites(spritesheet.ghostNumberSprites())
			.end();
		//@formatter:on

		animationsByName.put(GhostAnimations.GHOST_NORMAL, normal);
		animationsByName.put(GhostAnimations.GHOST_FRIGHTENED, frightened);
		animationsByName.put(GhostAnimations.GHOST_FLASHING, flashing);
		animationsByName.put(GhostAnimations.GHOST_EYES, eyes);
		animationsByName.put(GhostAnimations.GHOST_NUMBER, number);

		// TODO check this
		eyes.start();
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
			currentAnimation.setSprites(spritesheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (GhostAnimations.GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.ghostEyesSprites(ghost.wishDir()));
		}
	}
}