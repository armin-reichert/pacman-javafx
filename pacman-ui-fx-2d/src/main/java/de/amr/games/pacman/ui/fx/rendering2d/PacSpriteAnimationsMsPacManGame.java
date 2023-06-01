/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacSpriteAnimationsMsPacManGame extends PacSpriteAnimationsCommon {

	public PacSpriteAnimationsMsPacManGame(Pac pac, SpritesheetMsPacManGame spritesheet) {
		super(pac, spritesheet);

		var husbandMunchingAnimation = SpriteAnimation.builder() //
				.frameDurationTicks(2) //
				.loop() //
				.sprites(spritesheet.pacManMunchingSprites(Direction.LEFT)) //
				.build();

		animationsByName.put(PacAnimations.HUSBAND_MUNCHING, husbandMunchingAnimation);
	}

	@Override
	public SpritesheetMsPacManGame spritesheet() {
		return (SpritesheetMsPacManGame) spritesheet;
	}

	@Override
	protected Rectangle2D[] munchingSprites(Direction dir) {
		return spritesheet().msPacManMunchingSprites(dir);
	}

	@Override
	protected Rectangle2D[] dyingSprites() {
		return spritesheet().msPacManDyingSprites();
	}

	@Override
	public Rectangle2D currentSprite() {
		if (HUSBAND_MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet().pacManMunchingSprites(pac.moveDir()));
		}
		return super.currentSprite();
	}

}