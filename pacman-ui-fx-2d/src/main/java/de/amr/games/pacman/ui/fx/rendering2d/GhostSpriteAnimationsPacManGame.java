/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostSpriteAnimationsPacManGame extends GhostSpriteAnimationsCommon {

	private SpriteAnimation damagedAnimation;
	private SpriteAnimation stretchedAnimation;
	private SpriteAnimation patchedAnimation;
	private SpriteAnimation nakedAnimation;

	public GhostSpriteAnimationsPacManGame(Ghost ghost, SpritesheetPacManGame spritesheet) {
		super(ghost, spritesheet);
		createBlinkyDamagedAnimation(spritesheet);
		createBlinkyStretchedAnimation(spritesheet);
		createBlinkyPatchedAnimation(spritesheet);
		createBlinkyNakedAnimation(spritesheet);
	}

	@Override
	public SpritesheetPacManGame spritesheet() {
		return (SpritesheetPacManGame) spritesheet;
	}

	@Override
	protected SpriteAnimation animationByName(String name) {
		if (BLINKY_DAMAGED.equals(name)) {
			return damagedAnimation;
		}
		if (BLINKY_NAKED.equals(name)) {
			return nakedAnimation;
		}
		if (BLINKY_PATCHED.equals(name)) {
			return patchedAnimation;
		}
		if (BLINKY_STRETCHED.equals(name)) {
			return stretchedAnimation;
		}
		return super.animationByName(name);
	}

	public SpriteAnimation getStretchedAnimation() {
		return stretchedAnimation;
	}

	public SpriteAnimation getDamagedAnimation() {
		return damagedAnimation;
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

	private void createBlinkyDamagedAnimation(SpritesheetPacManGame ss) {
		damagedAnimation = new SpriteAnimation.Builder() //
				.sprites(ss.blinkyDamagedSprites()) //
				.build();
	}

	private void createBlinkyStretchedAnimation(SpritesheetPacManGame ss) {
		stretchedAnimation = new SpriteAnimation.Builder() //
				.sprites(ss.blinkyStretchedSprites()) //
				.build();
	}

	private void createBlinkyPatchedAnimation(SpritesheetPacManGame ss) {
		patchedAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(4) //
				.loop() //
				.sprites(ss.blinkyPatchedSprites()) //
				.build();
	}

	private void createBlinkyNakedAnimation(SpritesheetPacManGame ss) {
		nakedAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(4) //
				.loop() //
				.sprites(ss.blinkyNakedSprites()) //
				.build();
	}
}