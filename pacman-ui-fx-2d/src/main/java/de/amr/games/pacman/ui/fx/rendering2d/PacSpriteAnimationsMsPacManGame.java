/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacSpriteAnimationsMsPacManGame extends PacSpriteAnimationsCommon {

	protected SpriteAnimation husbandMunchingAnimation;

	public PacSpriteAnimationsMsPacManGame(Pac pac, SpritesheetMsPacManGame spritesheet) {
		super(pac, spritesheet);
		husbandMunchingAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(2) //
				.loop() //
				.sprites(spritesheet.pacManMunchingSprites(Direction.LEFT)) //
				.build();
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
	protected SpriteAnimation animation(String name, Direction dir) {
		if (HUSBAND_MUNCHING.equals(name)) {
			return husbandMunchingAnimation;
		}
		return super.animation(name, dir);
	}

	@Override
	public Rectangle2D currentSprite() {
		if (currentAnimation == husbandMunchingAnimation) {
			husbandMunchingAnimation.setSprites(spritesheet().pacManMunchingSprites(pac.moveDir()));
		}
		return super.currentSprite();
	}

}