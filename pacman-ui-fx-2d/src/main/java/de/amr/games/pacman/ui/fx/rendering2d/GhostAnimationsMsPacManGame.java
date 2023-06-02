/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimationsMsPacManGame extends SpriteAnimations
		implements GhostAnimations<SpriteAnimation, Rectangle2D> {

	private final Ghost ghost;
	private final SpritesheetMsPacManGame spritesheet;

	public GhostAnimationsMsPacManGame(Ghost ghost, SpritesheetMsPacManGame spritesheet) {
		Globals.checkNotNull(ghost);
		Globals.checkNotNull(spritesheet);
		this.ghost = ghost;
		this.spritesheet = spritesheet;

		//@formatter:off
		var normal = SpriteAnimation.begin().sprites(spritesheet.ghostNormalSprites(ghost.id(), Direction.LEFT)).frameTicks(8).loop().end();
		var frightened = SpriteAnimation.begin().sprites(spritesheet.ghostFrightenedSprites()).frameTicks(8).loop().end();
		var flashing = SpriteAnimation.begin().sprites(spritesheet.ghostFlashingSprites()).frameTicks(6).loop().end();
		var eyesAnimation = SpriteAnimation.begin().sprites(spritesheet.ghostEyesSprites(Direction.LEFT)).end();
		var numberAnimation = SpriteAnimation.begin().sprites(spritesheet.ghostNumberSprites()).end();
		//@formatter:on

		animationsByName.put(GHOST_NORMAL, normal);
		animationsByName.put(GHOST_FRIGHTENED, frightened);
		animationsByName.put(GHOST_FLASHING, flashing);
		animationsByName.put(GHOST_EYES, eyesAnimation);
		animationsByName.put(GHOST_NUMBER, numberAnimation);

		// TODO check this
		eyesAnimation.start();
		frightened.start();
		flashing.start();
	}

	@Override
	public Rectangle2D currentSprite() {
		if (currentAnimationName == null) {
			return null;
		}
		if (GHOST_NORMAL.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation.setSprites(spritesheet.ghostEyesSprites(ghost.wishDir()));
		}
		return currentAnimation.currentSprite();
	}
}