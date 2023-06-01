/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class SpritesheetPacManGame extends Spritesheet {

	public static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	public SpritesheetPacManGame(Image source, int raster) {
		super(source, raster);
	}

	private Rectangle2D[] ghostNumberSprites;

	public Rectangle2D[] ghostNumberSprites() {
		if (ghostNumberSprites == null) {
			ghostNumberSprites = tilesRightOf(0, 8, 4);
		}
		return ghostNumberSprites;
	}

	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tile(2 + symbol, 3);
	}

	public Rectangle2D bonusValueSprite(int symbol) {
		switch (symbol) {
		case 0:
			return region(0, 148, 16, 8); // 100
		case 1:
			return region(16, 148, 16, 8); // 300
		case 2:
			return region(32, 148, 16, 8); // 500
		case 3:
			return region(48, 148, 16, 8); // 700
		case 4:
			return region(64, 148, 18, 8); // 1000
		case 5:
			return region(62, 164, 20, 8); // 2000
		case 6:
			return region(62, 180, 20, 8); // 3000
		case 7:
			return region(62, 196, 20, 8); // 5000
		default:
			return null;
		}
	}

	public Rectangle2D ghostFacingRight(int ghostID) {
		return tile(2 * DIR_ORDER.index(Direction.RIGHT), 4 + ghostID);
	}

	public Rectangle2D livesCounterSprite() {
		return region(129, 15, 16, 16);
	}

	private Rectangle2D[][] pacMunchingSprites = new Rectangle2D[4][];

	public Rectangle2D[] pacMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		if (pacMunchingSprites[d] == null) {
			var wide = region(0, d * 16, 14, 14);
			var middle = region(16, d * 16, 14, 14);
			var closed = region(32, 0, 14, 14);
			pacMunchingSprites[d] = new Rectangle2D[] { closed, closed, middle, middle, wide, wide, middle, middle };
		}
		return pacMunchingSprites[d];
	}

	private Rectangle2D[] pacDyingSprites;

	public Rectangle2D[] pacDyingSprites() {
		if (pacDyingSprites == null) {
			pacDyingSprites = new Rectangle2D[11];
			for (int i = 0; i < 11; ++i) {
				pacDyingSprites[i] = new Rectangle2D(48 + i * 16, 0, 15, 15);
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
			ghostFrightenedSprites = new Rectangle2D[] { tile(8, 4), tile(9, 4) };
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
			ghostEyesSprites[d] = new Rectangle2D[] { tile(8 + d, 5) };
		}
		return ghostEyesSprites[d];
	}

	// Pac-Man specific:

	public Rectangle2D[] bigPacManSprites() {
		return new Rectangle2D[] { region(32, 16, 32, 32), region(64, 16, 32, 32), region(96, 16, 32, 32) };
	}

	public Rectangle2D[] blinkyDamagedSprites() {
		return new Rectangle2D[] { tile(8, 7), tile(9, 7) };
	}

	public Rectangle2D[] blinkyStretchedSprites() {
		return tilesRightOf(8, 6, 5);
	}

	public Rectangle2D[] blinkyPatchedSprites() {
		return tilesRightOf(10, 7, 2);
	}

	public Rectangle2D[] blinkyNakedSprites() {
		return new Rectangle2D[] { tiles(8, 8, 2, 1), tiles(10, 8, 2, 1) };
	}
}