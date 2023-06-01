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

	protected SpriteAnimation husbandMunching;

	public PacSpriteAnimationsMsPacManGame(Pac pac, SpritesheetMsPacManGame spritesheet) {
		super(pac, spritesheet);
		husbandMunching = new SpriteAnimation.Builder() //
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
	protected Rectangle2D[] pacMunchingSprites(Direction dir) {
		return spritesheet().pacMunchingSprites(dir);
	}

	@Override
	protected Rectangle2D[] pacDyingSprites() {
		return spritesheet().pacDyingSprites();
	}
}