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
public class PacSpriteAnimationsPacManGame extends PacSpriteAnimationsCommon {

	protected SpriteAnimation bigPacManAnimation;

	public PacSpriteAnimationsPacManGame(Pac pac, SpritesheetPacManGame spritesheet) {
		super(pac, spritesheet);
		bigPacManAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(3) //
				.loop() //
				.sprites(spritesheet.bigPacManSprites()) //
				.build();
	}

	@Override
	public SpritesheetPacManGame spritesheet() {
		return (SpritesheetPacManGame) spritesheet;
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