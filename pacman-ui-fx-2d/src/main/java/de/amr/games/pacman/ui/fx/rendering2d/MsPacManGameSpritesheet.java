/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.anim.AnimationByDirection;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class MsPacManGameSpritesheet extends Spritesheet implements GameSpritesheet {

	private static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private static final int MAZE_IMAGE_WIDTH = 226;
	private static final int MAZE_IMAGE_HEIGHT = 248;
	private static final int SECOND_COLUMN = 228;
	private static final int THIRD_COLUMN = 456;

	public MsPacManGameSpritesheet(Image source, int raster) {
		super(source, raster);
	}

	@Override
	public Spritesheet sheet() {
		return this;
	}

	private Rectangle2D tileFromThirdColumn(int tileX, int tileY) {
		return tilesFrom(THIRD_COLUMN, 0, tileX, tileY, 1, 1);
	}

	@Override
	public Rectangle2D[] ghostNumberSprites() {
		return new Rectangle2D[] { tileFromThirdColumn(0, 8), tileFromThirdColumn(1, 8), tileFromThirdColumn(2, 8),
				tileFromThirdColumn(3, 8) };
	}

	@Override
	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tileFromThirdColumn(3 + symbol, 0);
	}

	@Override
	public Rectangle2D bonusValueSprite(int symbol) {
		return tileFromThirdColumn(3 + symbol, 1);
	}

	@Override
	public Rectangle2D livesCounterSprite() {
		return tileFromThirdColumn(1, 0);
	}

	public Rectangle2D highlightedMaze(int mazeNumber) {
		return new Rectangle2D(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
	}

	public Rectangle2D emptyMaze(int mazeNumber) {
		return region(SECOND_COLUMN, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
	}

	public Rectangle2D filledMaze(int mazeNumber) {
		return region(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
	}

	@Override
	public Rectangle2D[] pacMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		var wide = tileFromThirdColumn(0, d);
		var middle = tileFromThirdColumn(1, d);
		var closed = tileFromThirdColumn(2, d);
		return new Rectangle2D[] { middle, middle, wide, wide, middle, middle, middle, closed, closed };
	}

	@Override
	public Rectangle2D[] pacDyingSprites() {
		var right = tileFromThirdColumn(1, 0);
		var left = tileFromThirdColumn(1, 1);
		var up = tileFromThirdColumn(1, 2);
		var down = tileFromThirdColumn(1, 3);
		// TODO not yet 100% accurate
		return new Rectangle2D[] { down, left, up, right, down, left, up, right, down, left, up };
	}

	@Override
	public Rectangle2D[] ghostNormalSprites(byte ghostID, Direction dir) {
		int d = DIR_ORDER.index(dir);
		return new Rectangle2D[] { tileFromThirdColumn(2 * d, 4 + ghostID), tileFromThirdColumn(2 * d + 1, 4 + ghostID) };
	}

	@Override
	public Rectangle2D[] ghostFrightenedSprites() {
		return new Rectangle2D[] { tileFromThirdColumn(8, 4), tileFromThirdColumn(9, 4) };
	}

	@Override
	public Rectangle2D[] ghostFlashingSprites() {
		return new Rectangle2D[] { tileFromThirdColumn(8, 4), tileFromThirdColumn(9, 4), tileFromThirdColumn(10, 4),
				tileFromThirdColumn(11, 4) };
	}

	@Override
	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		return new Rectangle2D[] { tileFromThirdColumn(8 + d, 5) };
	}

	// Ms. Pac-Man specific:

	public Rectangle2D heartSprite() {
		return tileFromThirdColumn(2, 10);
	}

	public Rectangle2D blueBagSprite() {
		return region(488, 199, 8, 8);
	}

	public Rectangle2D juniorPacSprite() {
		return region(509, 200, 8, 8);
	}

	public AnimationByDirection createPacManMunchingAnimationMap(Pac pac) {
		var animationByDir = new AnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var animation = new SimpleAnimation<>(tileFromThirdColumn(0, 9 + d), tileFromThirdColumn(1, 9 + d),
					tileFromThirdColumn(2, 9));
			animation.setFrameDuration(2);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	public SimpleAnimation<Rectangle2D> createClapperboardAnimation() {
		// TODO this is not 100% accurate yet
		var animation = new SimpleAnimation<>( //
				region(456, 208, 32, 32), //
				region(488, 208, 32, 32), //
				region(520, 208, 32, 32), //
				region(488, 208, 32, 32), //
				region(456, 208, 32, 32)//
		);
		animation.setFrameDuration(4);
		return animation;
	}

	public SimpleAnimation<Rectangle2D> createStorkFlyingAnimation() {
		var animation = new SimpleAnimation<>( //
				region(489, 176, 32, 16), //
				region(521, 176, 32, 16) //
		);
		animation.repeatForever();
		animation.setFrameDuration(8);
		return animation;
	}
}