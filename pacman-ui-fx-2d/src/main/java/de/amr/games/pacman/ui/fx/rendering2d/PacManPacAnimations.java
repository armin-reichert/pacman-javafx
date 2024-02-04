/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManPacAnimations extends SpriteAnimations {

	private final Pac pac;
	private final PacManSpriteSheet spriteSheet;

	public PacManPacAnimations(Pac pac, PacManSpriteSheet spriteSheet) {
		checkNotNull(pac);
		checkNotNull(spriteSheet);
		this.pac = pac;
		this.spriteSheet = spriteSheet;

		var munching = SpriteAnimation
			.begin()
				.sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
				.loop()
			.end();
		
		var dying = SpriteAnimation
			.begin()
				.sprites(spriteSheet.pacDyingSprites())
				.frameTicks(8)
			.end();
		
		var bigPacMan = SpriteAnimation
			.begin()
				.sprites(spriteSheet.bigPacManSprites())
				.frameTicks(3)
				.loop()
			.end();

		animationsByName.put(PacAnimations.MUNCHING,   munching);
		animationsByName.put(PacAnimations.DYING,      dying);
		animationsByName.put(PacAnimations.BIG_PACMAN, bigPacMan);
	}

	@Override
	public void updateCurrentAnimation() {
		if (PacAnimations.MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.pacMunchingSprites(pac.moveDir()));
		}
	}
}