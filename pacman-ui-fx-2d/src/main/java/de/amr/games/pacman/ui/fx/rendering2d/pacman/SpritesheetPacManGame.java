/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class SpritesheetPacManGame implements Spritesheet {

	public static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private final Image source;

	public SpritesheetPacManGame(Image source) {
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

	private Rectangle2D[] ghostNumberSprites;

	public Rectangle2D[] ghostNumberSprites() {
		if (ghostNumberSprites == null) {
			ghostNumberSprites = array(rect(0, 132, 16, 8), rect(16, 132, 16, 8), rect(32, 132, 16, 8), rect(48, 132, 16, 8));
		}
		return ghostNumberSprites;
	}

	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tile(2 + symbol, 3);
	}

	public Rectangle2D bonusValueSprite(int symbol) {
		switch (symbol) {
		case 0:
			return rect(0, 148, 16, 8); // 100
		case 1:
			return rect(16, 148, 16, 8); // 300
		case 2:
			return rect(32, 148, 16, 8); // 500
		case 3:
			return rect(48, 148, 16, 8); // 700
		case 4:
			return rect(64, 148, 18, 8); // 1000
		case 5:
			return rect(62, 164, 20, 8); // 2000
		case 6:
			return rect(62, 180, 20, 8); // 3000
		case 7:
			return rect(62, 196, 20, 8); // 5000
		default:
			return null;
		}
	}

	public Rectangle2D ghostFacingRight(int ghostID) {
		return tile(2 * DIR_ORDER.index(Direction.RIGHT), 4 + ghostID);
	}

	public Rectangle2D livesCounterSprite() {
		return rect(129, 15, 16, 16);
	}

	private Rectangle2D[][] pacMunchingSprites = new Rectangle2D[4][];

	public Rectangle2D[] pacMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (pacMunchingSprites[d] == null) {
			double m = 0.5; // margin
			double size = 16 - 2 * m;
			var wide = rect(0 + m, d * 16 + m, size, size);
			var middle = rect(16 + m, d * 16 + m, size, size);
			var closed = rect(32 + m, 0 + m, size, size);
			pacMunchingSprites[d] = array(closed, closed, middle, middle, wide, wide, middle, middle);
		}
		return pacMunchingSprites[d];
	}

	private Rectangle2D[] pacDyingSprites;

	public Rectangle2D[] pacDyingSprites() {
		if (pacDyingSprites == null) {
			double m = 0.5; // margin
			double size = 16 - 2 * m;
			pacDyingSprites = new Rectangle2D[11];
			for (int i = 0; i < 11; ++i) {
				pacDyingSprites[i] = new Rectangle2D(48 + i * 16 + m, m, size, size);
			}
		}
		return pacDyingSprites;
	}

	private Rectangle2D[][][] ghostNormalSprites = new Rectangle2D[4][4][];

	public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (ghostNormalSprites[id][d] == null) {
			ghostNormalSprites[id][d] = tilesRightOf(2 * d, 4 + id, 2);
		}
		return ghostNormalSprites[id][d];
	}

	private Rectangle2D[] ghostFrightenedSprites;

	public Rectangle2D[] ghostFrightenedSprites() {
		if (ghostFrightenedSprites == null) {
			ghostFrightenedSprites = array(tile(8, 4), tile(9, 4));
		}
		return ghostFrightenedSprites;
	}

	private Rectangle2D[] ghostFlashingSprites;

	public Rectangle2D[] ghostFlashingSprites() {
		if (ghostFlashingSprites == null) {
			ghostFlashingSprites = tilesRightOf(8, 4, 4);
		}
		return ghostFlashingSprites;
	}

	private Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];

	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (ghostEyesSprites[d] == null) {
			ghostEyesSprites[d] = array(tile(8 + d, 5));
		}
		return ghostEyesSprites[d];
	}

	// Pac-Man specific:

	public Rectangle2D[] bigPacManSprites() {
		return array(rect(32, 16, 32, 32), rect(64, 16, 32, 32), rect(96, 16, 32, 32));
	}

	public Rectangle2D[] blinkyDamagedSprites() {
		return array(tile(8, 7), tile(9, 7));
	}

	public Rectangle2D[] blinkyStretchedSprites() {
		return tilesRightOf(8, 6, 5);
	}

	public Rectangle2D[] blinkyPatchedSprites() {
		return tilesRightOf(10, 7, 2);
	}

	public Rectangle2D[] blinkyNakedSprites() {
		return array(rect(r(8), r(8), r(2), r(1)), rect(r(10), r(8), r(2), r(1)));
	}
}