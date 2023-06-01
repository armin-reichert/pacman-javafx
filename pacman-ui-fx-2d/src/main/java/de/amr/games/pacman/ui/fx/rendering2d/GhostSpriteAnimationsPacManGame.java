/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostSpriteAnimationsPacManGame extends GhostSpriteAnimationsCommon {

	public GhostSpriteAnimationsPacManGame(Ghost ghost, SpritesheetPacManGame spritesheet) {
		super(ghost, spritesheet);

		var damaged = SpriteAnimation.begin().sprites(spritesheet.blinkyDamagedSprites()).end();
		var stretched = SpriteAnimation.begin().sprites(spritesheet.blinkyStretchedSprites()).end();
		var patched = SpriteAnimation.begin().frameTicks(4).loop().sprites(spritesheet.blinkyPatchedSprites()).end();
		var naked = SpriteAnimation.begin().frameTicks(4).loop().sprites(spritesheet.blinkyNakedSprites()).end();

		animationsByName.put(GhostAnimations.BLINKY_DAMAGED, damaged);
		animationsByName.put(GhostAnimations.BLINKY_STRETCHED, stretched);
		animationsByName.put(GhostAnimations.BLINKY_PATCHED, patched);
		animationsByName.put(GhostAnimations.BLINKY_NAKED, naked);
	}

	@Override
	public SpritesheetPacManGame spritesheet() {
		return (SpritesheetPacManGame) spritesheet;
	}

	@Override
	protected Rectangle2D[] normalSprites(byte id, Direction dir) {
		return spritesheet().ghostNormalSprites(id, dir);
	}

	@Override
	protected Rectangle2D[] frightenedSprites() {
		return spritesheet().ghostFrightenedSprites();
	}

	@Override
	protected Rectangle2D[] flashingSprites() {
		return spritesheet().ghostFlashingSprites();
	}

	@Override
	protected Rectangle2D[] eyesSprites(Direction dir) {
		return spritesheet().ghostEyesSprites(dir);
	}

	@Override
	protected Rectangle2D[] numberSprites() {
		return spritesheet().ghostNumberSprites();
	}

}