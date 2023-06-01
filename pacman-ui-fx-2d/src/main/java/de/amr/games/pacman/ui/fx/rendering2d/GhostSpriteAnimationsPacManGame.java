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

		var damagedAnimation = new SpriteAnimation.Builder() //
				.sprites(spritesheet.blinkyDamagedSprites()) //
				.build();

		var stretchedAnimation = new SpriteAnimation.Builder() //
				.sprites(spritesheet.blinkyStretchedSprites()) //
				.build();

		var patchedAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(4) //
				.loop() //
				.sprites(spritesheet.blinkyPatchedSprites()) //
				.build();

		var nakedAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(4) //
				.loop() //
				.sprites(spritesheet.blinkyNakedSprites()) //
				.build();

		animationsByName.put(GhostAnimations.BLINKY_DAMAGED, damagedAnimation);
		animationsByName.put(GhostAnimations.BLINKY_STRETCHED, stretchedAnimation);
		animationsByName.put(GhostAnimations.BLINKY_PATCHED, patchedAnimation);
		animationsByName.put(GhostAnimations.BLINKY_NAKED, nakedAnimation);
	}

	@Override
	public SpritesheetPacManGame spritesheet() {
		return (SpritesheetPacManGame) spritesheet;
	}

	@Override
	protected Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
		return spritesheet().ghostNormalSprites(id, dir);
	}

	@Override
	protected Rectangle2D[] ghostFrightenedSprites() {
		return spritesheet().ghostFrightenedSprites();
	}

	@Override
	protected Rectangle2D[] ghostFlashingSprites() {
		return spritesheet().ghostFlashingSprites();
	}

	@Override
	protected Rectangle2D[] ghostEyesSprites(Direction dir) {
		return spritesheet().ghostEyesSprites(dir);
	}

	@Override
	protected Rectangle2D[] ghostNumberSprites() {
		return spritesheet().ghostNumberSprites();
	}

}