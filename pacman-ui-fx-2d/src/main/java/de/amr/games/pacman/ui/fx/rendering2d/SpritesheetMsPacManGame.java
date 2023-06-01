/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class SpritesheetMsPacManGame extends Spritesheet {

	private static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private static final int MAZE_IMAGE_WIDTH = 226;
	private static final int MAZE_IMAGE_HEIGHT = 248;
	private static final int SECOND_COLUMN = 228;
	private static final int THIRD_COLUMN = 456;

	public SpritesheetMsPacManGame(Image source, int raster) {
		super(source, raster);
	}

	private Rectangle2D tileFromThirdColumn(int tileX, int tileY) {
		return tilesFrom(THIRD_COLUMN, 0, tileX, tileY, 1, 1);
	}

	public Rectangle2D[] ghostNumberSprites() {
		return new Rectangle2D[] { tileFromThirdColumn(0, 8), tileFromThirdColumn(1, 8), tileFromThirdColumn(2, 8),
				tileFromThirdColumn(3, 8) };
	}

	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tileFromThirdColumn(3 + symbol, 0);
	}

	public Rectangle2D bonusValueSprite(int symbol) {
		return tileFromThirdColumn(3 + symbol, 1);
	}

	public Rectangle2D livesCounterSprite() {
		return tileFromThirdColumn(1, 0);
	}

	private Rectangle2D[][] msPacManMunchingSprites = new Rectangle2D[4][];

	public Rectangle2D[] msPacManMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (msPacManMunchingSprites[d] == null) {
			var wide = tileFromThirdColumn(0, d);
			var middle = tileFromThirdColumn(1, d);
			var closed = tileFromThirdColumn(2, d);
			msPacManMunchingSprites[d] = new Rectangle2D[] { middle, middle, wide, wide, middle, middle, middle, closed,
					closed };
		}
		return msPacManMunchingSprites[d];
	}

	private Rectangle2D[] msPacManDyingSprites;

	public Rectangle2D[] msPacManDyingSprites() {
		if (msPacManDyingSprites == null) {
			var right = tileFromThirdColumn(1, 0);
			var left = tileFromThirdColumn(1, 1);
			var up = tileFromThirdColumn(1, 2);
			var down = tileFromThirdColumn(1, 3);
			// TODO not yet 100% accurate
			msPacManDyingSprites = new Rectangle2D[] { down, left, up, right, down, left, up, right, down, left, up };
		}
		return msPacManDyingSprites;
	}

	private Rectangle2D[][][] ghostNormalSprites = new Rectangle2D[4][4][];

	public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (ghostNormalSprites[id][d] == null) {
			ghostNormalSprites[id][d] = new Rectangle2D[] { tileFromThirdColumn(2 * d, 4 + id),
					tileFromThirdColumn(2 * d + 1, 4 + id) };
		}
		return ghostNormalSprites[id][d];
	}

	private Rectangle2D[] ghostFrightenedSprites;

	public Rectangle2D[] ghostFrightenedSprites() {
		if (ghostFrightenedSprites == null) {
			ghostFrightenedSprites = new Rectangle2D[] { tileFromThirdColumn(8, 4), tileFromThirdColumn(9, 4) };
		}
		return ghostFrightenedSprites;
	}

	private Rectangle2D[] ghostFlashingSprites;

	public Rectangle2D[] ghostFlashingSprites() {
		if (ghostFlashingSprites == null) {
			ghostFlashingSprites = new Rectangle2D[] { tileFromThirdColumn(8, 4), tileFromThirdColumn(9, 4),
					tileFromThirdColumn(10, 4), tileFromThirdColumn(11, 4) };
		}
		return ghostFlashingSprites;
	}

	private Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];

	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (ghostEyesSprites[d] == null) {
			ghostEyesSprites[d] = new Rectangle2D[] { tileFromThirdColumn(8 + d, 5) };
		}
		return ghostEyesSprites[d];
	}

	// Ms. Pac-Man specific:

	private static final int MS_PACMAN_MAZE_COUNT = 6;

	private Rectangle2D[] highlightedMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

	public Rectangle2D highlightedMaze(int mazeNumber) {
		if (highlightedMazeSprites[mazeNumber - 1] == null) {
			highlightedMazeSprites[mazeNumber - 1] = new Rectangle2D(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
					MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
		}
		return highlightedMazeSprites[mazeNumber - 1];
	}

	private Rectangle2D[] emptyMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

	public Rectangle2D emptyMaze(int mazeNumber) {
		if (emptyMazeSprites[mazeNumber - 1] == null) {
			emptyMazeSprites[mazeNumber - 1] = region(SECOND_COLUMN, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH,
					MAZE_IMAGE_HEIGHT);
		}
		return emptyMazeSprites[mazeNumber - 1];
	}

	private Rectangle2D[] filledMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

	public Rectangle2D filledMaze(int mazeNumber) {
		if (filledMazeSprites[mazeNumber - 1] == null) {
			filledMazeSprites[mazeNumber - 1] = region(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH,
					MAZE_IMAGE_HEIGHT);
		}
		return filledMazeSprites[mazeNumber - 1];
	}

	private Rectangle2D[][] pacManMunchingSprites = new Rectangle2D[4][];

	public Rectangle2D[] pacManMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (pacManMunchingSprites[d] == null) {
			pacManMunchingSprites[d] = new Rectangle2D[] { tileFromThirdColumn(0, 9 + d), tileFromThirdColumn(1, 9 + d),
					tileFromThirdColumn(2, 9) };
		}
		return pacManMunchingSprites[d];
	}

	public Rectangle2D heartSprite() {
		return tileFromThirdColumn(2, 10);
	}

	public Rectangle2D blueBagSprite() {
		return region(488, 199, 8, 8);
	}

	public Rectangle2D juniorPacSprite() {
		return region(509, 200, 8, 8);
	}

	// TODO this is not 100% accurate yet
	public SpriteAnimation createClapperboardAnimation() {
		return SpriteAnimation.builder() //
				.frameDurationTicks(4) //
				.sprites(//
						region(456, 208, 32, 32), //
						region(488, 208, 32, 32), //
						region(520, 208, 32, 32), //
						region(488, 208, 32, 32), //
						region(456, 208, 32, 32)//
				) //
				.build();
	}

	public SpriteAnimation createStorkFlyingAnimation() {
		return SpriteAnimation.builder() //
				.frameDurationTicks(8) //
				.loop() //
				.sprites(//
						region(489, 176, 32, 16), //
						region(521, 176, 32, 16) //
				).build();
	}
}