/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.mspacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class SpritesheetMsPacManGame implements Spritesheet {

	private static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private static final int MAZE_IMAGE_WIDTH = 226;
	private static final int MAZE_IMAGE_HEIGHT = 248;

	private static final int SECOND_COLUMN = 228;
	private static final int THIRD_COLUMN = 456;

	private final Image source;

	public SpritesheetMsPacManGame(Image source) {
		this.source = source;
	}

	@Override
	public Image source() {
		return source;
	}

	@Override
	public int raster() {
		return 16;
	}

	// third column contains the sprites (first two columns the maze images)
	private Rectangle2D sprite(int tileX, int tileY) {
		double offsetX = THIRD_COLUMN;
		double offsetY = 0;
		return new Rectangle2D(offsetX + r(tileX), offsetY + r(tileY), raster(), raster());
	}

	private Rectangle2D[] ghostNumberSprites;

	public Rectangle2D[] ghostNumberSprites() {
		if (ghostNumberSprites == null) {
			ghostNumberSprites = array(sprite(0, 8), sprite(1, 8), sprite(2, 8), sprite(3, 8));
		}
		return ghostNumberSprites;
	}

	public Rectangle2D bonusSymbolSprite(int symbol) {
		return sprite(3 + symbol, 0);
	}

	public Rectangle2D bonusValueSprite(int symbol) {
		return sprite(3 + symbol, 1);
	}

	public Rectangle2D livesCounterSprite() {
		return sprite(1, 0);
	}

	private Rectangle2D[][] msPacManMunchingSprites = new Rectangle2D[4][];

	public Rectangle2D[] msPacManMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (msPacManMunchingSprites[d] == null) {
			var wide = sprite(0, d);
			var open = sprite(1, d);
			var closed = sprite(2, d);
			msPacManMunchingSprites[d] = array(open, open, wide, wide, open, open, open, closed, closed);
		}
		return msPacManMunchingSprites[d];
	}

	private Rectangle2D[] msPacManDyingSprites;

	public Rectangle2D[] msPacManDyingSprites() {
		if (msPacManDyingSprites == null) {
			var right = sprite(1, 0);
			var left = sprite(1, 1);
			var up = sprite(1, 2);
			var down = sprite(1, 3);
			// TODO not yet 100% accurate
			msPacManDyingSprites = array(down, left, up, right, down, left, up, right, down, left, up);
		}
		return msPacManDyingSprites;
	}

	private Rectangle2D[][][] ghostsNormalSprites = new Rectangle2D[4][4][];

	public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (ghostsNormalSprites[id][d] == null) {
			ghostsNormalSprites[id][d] = array(sprite(2 * d, 4 + id), sprite(2 * d + 1, 4 + id));
		}
		return ghostsNormalSprites[id][d];
	}

	private Rectangle2D[] ghostFrightenedSprites;

	public Rectangle2D[] ghostFrightenedSprites() {
		if (ghostFrightenedSprites == null) {
			ghostFrightenedSprites = array(sprite(8, 4), sprite(9, 4));
		}
		return ghostFrightenedSprites;
	}

	private Rectangle2D[] ghostFlashingSprites;

	public Rectangle2D[] ghostFlashingSprites() {
		if (ghostFlashingSprites == null) {
			ghostFlashingSprites = array(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4));
		}
		return ghostFlashingSprites;
	}

	private Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];

	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (ghostEyesSprites[d] == null) {
			ghostEyesSprites[d] = array(sprite(8 + d, 5));
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
		int i = mazeNumber - 1;
		if (emptyMazeSprites[i] == null) {
			emptyMazeSprites[i] = rect(SECOND_COLUMN, i * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
		}
		return emptyMazeSprites[i];
	}

	private Rectangle2D[] filledMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

	public Rectangle2D filledMaze(int mazeNumber) {
		int i = mazeNumber - 1;
		if (filledMazeSprites[i] == null) {
			filledMazeSprites[i] = rect(0, i * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
		}
		return filledMazeSprites[i];
	}

	private Rectangle2D[][] pacManMunchingSprites = new Rectangle2D[4][];

	public Rectangle2D[] pacManMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (pacManMunchingSprites[d] == null) {
			pacManMunchingSprites[d] = array(sprite(0, 9 + d), sprite(1, 9 + d), sprite(2, 9));
		}
		return pacManMunchingSprites[d];
	}

	public Rectangle2D heartSprite() {
		return sprite(2, 10);
	}

	public Rectangle2D blueBagSprite() {
		return rect(488, 199, 8, 8);
	}

	public Rectangle2D juniorPacSprite() {
		return rect(509, 200, 8, 8);
	}

	// TODO this is not 100% accurate yet
	public SpriteAnimation createClapperboardAnimation() {
		return SpriteAnimation.begin() //
				.sprites(//
						rect(456, 208, 32, 32), //
						rect(488, 208, 32, 32), //
						rect(520, 208, 32, 32), //
						rect(488, 208, 32, 32), //
						rect(456, 208, 32, 32))//
				.frameTicks(4) //
				.end();
	}

	public SpriteAnimation createStorkFlyingAnimation() {
		return SpriteAnimation.begin() //
				.sprites(//
						rect(489, 176, 32, 16), //
						rect(521, 176, 32, 16)) //
				.frameTicks(8) //
				.loop() //
				.end();
	}
}