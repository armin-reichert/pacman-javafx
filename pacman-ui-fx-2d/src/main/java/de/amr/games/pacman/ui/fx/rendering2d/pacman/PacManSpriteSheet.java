/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class PacManSpriteSheet implements SpriteSheet {

	private static final Order<Direction> DIR_ORDER = new Order<>(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private final Image source;

	public PacManSpriteSheet(Image source) {
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

	private final Rectangle2D[] ghostNumberSprites = array(
		rect( 0, 132, 16, 8),  // 200
		rect(16, 132, 16, 8),  // 400
		rect(32, 132, 16, 8),  // 800
		rect(48, 132, 16, 8)); // 1600

	public Rectangle2D[] ghostNumberSprites() {
		return ghostNumberSprites;
	}

	private final Rectangle2D[] bonusSymbolSprites = IntStream.range(0, 8)
		.mapToObj(symbol -> tile(2 + symbol, 3))
		.toArray(Rectangle2D[]::new);

	public Rectangle2D bonusSymbolSprite(int symbol) {
		return bonusSymbolSprites[symbol];
	}

	private final Rectangle2D[] bonusValueSprites = new Rectangle2D[8];
	{
		for (byte symbol = 0; symbol < 8; ++symbol) {
			bonusValueSprites[symbol] = switch (symbol) {
				case 0 -> rect(0, 148, 16, 8); //  100
				case 1 -> rect(16, 148, 16, 8); //  300
				case 2 -> rect(32, 148, 16, 8); //  500
				case 3 -> rect(48, 148, 16, 8); //  700
				case 4 -> rect(64, 148, 18, 8); // 1000
				case 5 -> rect(62, 164, 20, 8); // 2000
				case 6 -> rect(62, 180, 20, 8); // 3000
				case 7 -> rect(62, 196, 20, 8); // 5000
				default -> null;
			};
		}
	}

	public Rectangle2D bonusValueSprite(int symbol) {
			return bonusValueSprites[symbol];
	}

	private final Rectangle2D[] ghostFacingRightSprites = new Rectangle2D[4];
	{
		for (byte id = 0; id < 4; ++id) {
			ghostFacingRightSprites[id] = tile(2 * DIR_ORDER.index(Direction.RIGHT), 4 + id);
		}
	}

	public Rectangle2D ghostFacingRight(int ghostID) {
		return ghostFacingRightSprites[ghostID];
	}

	private final Rectangle2D livesCounterSprite = rect(129, 15, 16, 16);

	public Rectangle2D livesCounterSprite() {
		return livesCounterSprite;
	}

	private final Rectangle2D[][] pacMunchingSprites = new Rectangle2D[4][];
	{
		double m = 0.5; // margin
		double size = 16 - 2 * m;
		for (byte d = 0; d < 4; ++d) {
			var wide = rect(0 + m, d * 16 + m, size, size);
			var middle = rect(16 + m, d * 16 + m, size, size);
			var closed = rect(32 + m, 0 + m, size, size);
			pacMunchingSprites[d] = array(closed, closed, middle, middle, wide, wide, middle, middle);
		}
	}

	public Rectangle2D[] pacMunchingSprites(Direction dir) {
		return pacMunchingSprites[DIR_ORDER.index(dir)];
	}

	private final Rectangle2D[] pacDyingSprites = new Rectangle2D[11];
	{
		double m = 0.5; // margin
		double size = 16 - 2 * m;
		for (int i = 0; i < 11; ++i) {
			pacDyingSprites[i] = new Rectangle2D(48 + i * 16 + m, m, size, size);
		}
	}

	public Rectangle2D[] pacDyingSprites() {
		return pacDyingSprites;
	}

	private final Rectangle2D[][][] ghostNormalSprites = new Rectangle2D[4][4][];
	{
		for (byte id = 0; id < 4; ++id) {
			for (byte d = 0; d < 4; ++d) {
				ghostNormalSprites[id][d] = tilesRightOf(2 * d, 4 + id, 2);
			}
		}
	}

	public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
		return ghostNormalSprites[id][DIR_ORDER.index(dir)];
	}

	private final Rectangle2D[] ghostFrightenedSprites = array(tile(8, 4), tile(9, 4));

	public Rectangle2D[] ghostFrightenedSprites() {
		return ghostFrightenedSprites;
	}

	private final Rectangle2D[] ghostFlashingSprites = tilesRightOf(8, 4, 4);

	public Rectangle2D[] ghostFlashingSprites() {
		return ghostFlashingSprites;
	}

	private final Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];
	{
		for (byte d = 0; d < 4; ++d) {
			ghostEyesSprites[d] = array(tile(8 + d, 5));
		}
	}

	public Rectangle2D[] ghostEyesSprites(Direction dir) {
		return ghostEyesSprites[DIR_ORDER.index(dir)];
	}

	// Pac-Man specific:

	private final Rectangle2D[] bigPacManSprites = array(
		rect(32, 16, 32, 32),
		rect(64, 16, 32, 32),
		rect(96, 16, 32, 32));

	public Rectangle2D[] bigPacManSprites() {
		return bigPacManSprites;
	}

	private final Rectangle2D[] blinkyDamagedSprites = array(tile(8, 7), tile(9, 7));

	public Rectangle2D[] blinkyDamagedSprites() {
		return blinkyDamagedSprites;
	}

	private final Rectangle2D[] blinkyStretchedSprites = tilesRightOf(8, 6, 5);

	public Rectangle2D[] blinkyStretchedSprites() {
		return blinkyStretchedSprites;
	}

	private final Rectangle2D[] blinkyPatchedSprites = tilesRightOf(10, 7, 2);

	public Rectangle2D[] blinkyPatchedSprites() {
		return blinkyPatchedSprites;
	}

	private final Rectangle2D[] blinkyNakedSprites = array(
		rect(r(8), r(8), r(2), r(1)), rect(r(10), r(8), r(2), r(1)));

	public Rectangle2D[] blinkyNakedSprites() {
		return blinkyNakedSprites;
	}
}