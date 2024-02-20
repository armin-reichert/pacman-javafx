/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class MsPacManGameGhostAnimations extends SpriteAnimations {

	private final Map<String, SpriteAnimation> animationsByName;
	private final Ghost ghost;
	private final MsPacManGameSpriteSheet spriteSheet;

	public MsPacManGameGhostAnimations(Ghost ghost, MsPacManGameSpriteSheet spriteSheet) {
		checkNotNull(ghost);
		checkNotNull(spriteSheet);
		this.ghost = ghost;
		this.spriteSheet = spriteSheet;

		var normal = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostNormalSprites(ghost.id(), Direction.LEFT))
			.frameTicks(8)
			.loop()
			.end();
		
		var frightened = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostFrightenedSprites())
			.frameTicks(8)
			.loop()
			.end();
		
		var flashing = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostFlashingSprites())
			.frameTicks(6)
			.loop()
			.end();
		
		var eyes = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
			.end();
		
		var number = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostNumberSprites())
			.end();

		animationsByName = Map.of(
			GhostAnimations.GHOST_NORMAL,     normal,
			GhostAnimations.GHOST_FRIGHTENED, frightened,
			GhostAnimations.GHOST_FLASHING,   flashing,
			GhostAnimations.GHOST_EYES,       eyes,
			GhostAnimations.GHOST_NUMBER,     number);

		// TODO check this
		eyes.start();
		frightened.start();
		flashing.start();
	}

	@Override
	public SpriteAnimation byName(String name) {
		return animationsByName.get(name);
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
			currentAnimation().setSprites(spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (GhostAnimations.GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation().setSprites(spriteSheet.ghostEyesSprites(ghost.wishDir()));
		}
	}
}