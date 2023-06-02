/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.mspacman;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacAnimationsMsPacManGame extends SpriteAnimations implements PacAnimations<SpriteAnimation, Rectangle2D> {

	private final Pac pac;
	private final SpritesheetMsPacManGame spritesheet;

	public PacAnimationsMsPacManGame(Pac pac, SpritesheetMsPacManGame spritesheet) {
		Globals.checkNotNull(pac);
		Globals.checkNotNull(spritesheet);
		this.pac = pac;
		this.spritesheet = spritesheet;
		//@formatter:off
		var munching = SpriteAnimation
			.begin()
				.sprites(spritesheet.msPacManMunchingSprites(Direction.LEFT))
				.loop()
			.end();
		
		var dying = SpriteAnimation
			.begin()
				.sprites(spritesheet.msPacManDyingSprites())
				.frameTicks(8)
			.end();
		
		var husbandMunching = SpriteAnimation
			.begin()
				.sprites(spritesheet.pacManMunchingSprites(Direction.LEFT))
				.frameTicks(2)
				.loop()
			.end();
		//@formatter:on
		animationsByName.put(PacAnimations.MUNCHING, munching);
		animationsByName.put(PacAnimations.DYING, dying);
		animationsByName.put(PacAnimations.HUSBAND_MUNCHING, husbandMunching);
	}

	@Override
	public void updateCurrentAnimation() {
		if (MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.msPacManMunchingSprites(pac.moveDir()));
		}
		if (HUSBAND_MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.pacManMunchingSprites(pac.moveDir()));
		}
	}
}