/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class PacSpriteAnimationsPacMan extends PacSpriteAnimations {

	protected SpriteAnimation bigPacManAnimation;

	public PacSpriteAnimationsPacMan(Pac pac, PacManGameSpritesheet spritesheet) {
		super(pac, spritesheet);
		bigPacManAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(3) //
				.loop() //
				.sprites(spritesheet.bigPacManSprites()) //
				.build();
	}

	@Override
	public PacManGameSpritesheet spritesheet() {
		return (PacManGameSpritesheet) spritesheet;
	}

	@Override
	protected Rectangle2D[] pacMunchingSprites(Direction dir) {
		return spritesheet().pacMunchingSprites(dir);
	}

	@Override
	protected Rectangle2D[] pacDyingSprites() {
		return spritesheet().pacDyingSprites();
	}

	@Override
	protected SpriteAnimation animation(String name, Direction dir) {
		if (BIG_PACMAN.equals(name)) {
			return bigPacManAnimation;
		}
		return super.animation(name, dir);
	}
}