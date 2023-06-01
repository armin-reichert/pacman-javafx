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
public class PacSpriteAnimationsPacManGame extends PacSpriteAnimationsCommon {

	public PacSpriteAnimationsPacManGame(Pac pac, SpritesheetPacManGame spritesheet) {
		super(pac, spritesheet);

		var bigPacManAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(3) //
				.loop() //
				.sprites(spritesheet.bigPacManSprites()) //
				.build();

		animationsByName.put(PacAnimations.BIG_PACMAN, bigPacManAnimation);
	}

	@Override
	public SpritesheetPacManGame spritesheet() {
		return (SpritesheetPacManGame) spritesheet;
	}

	@Override
	protected Rectangle2D[] munchingSprites(Direction dir) {
		return spritesheet().pacMunchingSprites(dir);
	}

	@Override
	protected Rectangle2D[] dyingSprites() {
		return spritesheet().pacDyingSprites();
	}
}