/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGamePacAnimations extends SpriteAnimations {

	private final Map<String, SpriteAnimation> animationsByName;
	private final Pac pac;
	private final PacManGameSpriteSheet spriteSheet;

	public PacManGamePacAnimations(Pac pac, PacManGameSpriteSheet spriteSheet) {
		checkNotNull(pac);
		checkNotNull(spriteSheet);
		this.pac = pac;
		this.spriteSheet = spriteSheet;

		var munching = SpriteAnimation.begin()
			.sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
			.loop()
			.end();
		
		var dying = SpriteAnimation.begin()
			.sprites(spriteSheet.pacDyingSprites())
			.frameTicks(8)
			.end();
		
		var bigPacMan = SpriteAnimation.begin()
			.sprites(spriteSheet.bigPacManSprites())
			.frameTicks(3)
			.loop()
			.end();

		animationsByName = Map.of(
			PacAnimations.MUNCHING,   munching,
			PacAnimations.DYING,      dying,
			PacAnimations.BIG_PACMAN, bigPacMan);
	}

	@Override
	public SpriteAnimation byName(String name) {
		return animationsByName.get(name);
	}

	@Override
	public void updateCurrentAnimation() {
		if (PacAnimations.MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.pacMunchingSprites(pac.moveDir()));
		}
	}
}