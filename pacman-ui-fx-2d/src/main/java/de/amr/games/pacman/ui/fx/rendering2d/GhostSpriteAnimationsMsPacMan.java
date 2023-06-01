/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostSpriteAnimationsMsPacMan extends GhostSpriteAnimations {

	public GhostSpriteAnimationsMsPacMan(Ghost ghost, MsPacManGameSpritesheet spritesheet) {
		super(ghost, spritesheet);
	}

	@Override
	public MsPacManGameSpritesheet spritesheet() {
		return (MsPacManGameSpritesheet) super.spritesheet();
	}

	@Override
	protected Rectangle2D[] ghostEyesSprites(Direction dir) {
		return spritesheet().ghostEyesSprites(dir);
	}

	@Override
	protected Rectangle2D[] ghostFlashingSprites() {
		return spritesheet().ghostFlashingSprites();
	}

	@Override
	protected Rectangle2D[] ghostFrightenedSprites() {
		return spritesheet().ghostFrightenedSprites();
	}

	@Override
	protected Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
		return spritesheet().ghostNormalSprites(id, dir);
	}

	@Override
	protected Rectangle2D[] ghostNumberSprites() {
		return spritesheet().ghostNumberSprites();
	}
}