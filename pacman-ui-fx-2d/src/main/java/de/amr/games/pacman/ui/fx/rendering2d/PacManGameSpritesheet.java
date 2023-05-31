/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class PacManGameSpritesheet extends GameSpritesheet {

	public static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	public PacManGameSpritesheet(Image source, int raster) {
		super(source, raster);
	}

	@Override
	public Rectangle2D[] ghostNumberSprites() {
		return tilesRightOf(0, 8, 4);
	}

	@Override
	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D bonusValueSprite(int symbol) {
		if (symbol <= 3) {
			return tile(symbol, 9);
		}
		if (symbol == 4) {
			var region = tiles(4, 9, 2, 1);
			return region(region.getMinX(), region.getMinY(), region.getWidth() - 13, region.getHeight()); // WTF
		}
		return tiles(3, 5 + symbol, 3, 1);
	}

	public Rectangle2D ghostFacingRight(int ghostID) {
		return tile(2 * DIR_ORDER.index(Direction.RIGHT), 4 + ghostID);
	}

	@Override
	public Rectangle2D livesCounterSprite() {
		return region(129, 15, 16, 16);
	}

	@Override
	public Rectangle2D[] pacMunchingSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		var wide = region(0, d * 16, 14, 14);
		var middle = region(16, d * 16, 14, 14);
		var closed = region(32, 0, 14, 14);
		return new Rectangle2D[] { closed, closed, middle, middle, wide, wide, middle, middle };
	}

	@Override
	public Rectangle2D[] pacDyingSprites() {
		var r = new Rectangle2D[11];
		for (int i = 0; i < 11; ++i) {
			r[i] = new Rectangle2D(48 + i * 16, 0, 15, 15);
		}
		return r;
//		return tilesRightOf(3, 0, 11);
	}

	@Override
	public Rectangle2D[] ghostNormalSprites(byte ghostID, Direction dir) {
		int d = DIR_ORDER.index(dir);
		return tilesRightOf(2 * d, 4 + ghostID, 2);
	}

	@Override
	public Rectangle2D[] ghostFrightenedSprites() {
		return new Rectangle2D[] { tile(8, 4), tile(9, 4) };
	}

	@Override
	public Rectangle2D[] ghostFlashingSprites() {
		return tilesRightOf(8, 4, 4);
	}

	@Override
	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		int d = DIR_ORDER.index(dir);
		return new Rectangle2D[] { tile(8 + d, 5) };
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