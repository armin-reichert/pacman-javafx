/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.fx.util.Order;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class PacManGameSpriteSheet implements SpriteSheet {

    private static final Order<Direction> DIR_ORDER = new Order<>(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);
    private static final int OFF_X = 456;

    private final Image source;
    private final Image flashingMazeImage;

    public PacManGameSpriteSheet(Image source, Image flashingMazeImage) {
        this.source = source;
        this.flashingMazeImage = flashingMazeImage;
    }

    @Override
    public Image source() {
        return source;
    }

    @Override
    public int raster() {
        return 16;
    }

    public Image getFlashingMazeImage() {
        return flashingMazeImage;
    }

    private final Rectangle2D fullMazeSprite = rect(0, 0, 224, 248);

    public Rectangle2D getFullMazeSprite() {
        return fullMazeSprite;
    }

    private final Rectangle2D emptyMazeSprite = rect(228, 0, 224, 248);

    public Rectangle2D getEmptyMazeSprite() {
        return emptyMazeSprite;
    }

    private final Rectangle2D energizerSprite = rect(8, 24, 8, 8);

    public Rectangle2D getEnergizerSprite() {
        return energizerSprite;
    }

    private final Rectangle2D[] ghostNumberSprites = array(
        rect(456, 133, 15, 7),  // 200
        rect(472, 133, 15, 7),  // 400
        rect(488, 133, 15, 7),  // 800
        rect(504, 133, 16, 7)); // 1600

    public Rectangle2D[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    private final Rectangle2D[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(sym -> rect(OFF_X + r(2 + sym), r(3) + 0.5, r(1), r(1) - 0.5))
        .toArray(Rectangle2D[]::new);

    public Rectangle2D bonusSymbolSprite(int symbol) {
        return bonusSymbolSprites[symbol];
    }

    private final Rectangle2D[] bonusValueSprites = new Rectangle2D[8];

    {
        for (byte symbol = 0; symbol < 8; ++symbol) {
            bonusValueSprites[symbol] = switch (symbol) {
                case 0 -> rect(457, 148, 14, 7); //  100
                case 1 -> rect(472, 148, 15, 7); //  300
                case 2 -> rect(488, 148, 15, 7); //  500
                case 3 -> rect(504, 148, 15, 7); //  700
                case 4 -> rect(520, 148, 18, 7); // 1000
                case 5 -> rect(518, 164, 20, 7); // 2000
                case 6 -> rect(518, 180, 20, 7); // 3000
                case 7 -> rect(518, 196, 20, 7); // 5000
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
            int rx = 2 * DIR_ORDER.index(Direction.RIGHT);
            int ry = 4 + id;
            ghostFacingRightSprites[id] = rect(OFF_X + r(rx), r(ry), raster(), raster());
        }
    }

    public Rectangle2D ghostFacingRight(int ghostID) {
        return ghostFacingRightSprites[ghostID];
    }

    private final Rectangle2D livesCounterSprite = rect(OFF_X + 129, 15, 16, 16);

    public Rectangle2D livesCounterSprite() {
        return livesCounterSprite;
    }

    private final Rectangle2D[][] pacMunchingSprites = new Rectangle2D[4][];

    {
        double m = 0.5; // margin
        double size = 16 - 2 * m;
        for (byte d = 0; d < 4; ++d) {
            var wide = rect(OFF_X + 0 + m, d * 16 + m, size, size);
            var middle = rect(OFF_X + 16 + m, d * 16 + m, size, size);
            var closed = rect(OFF_X + 32 + m, 0 + m, size, size);
            pacMunchingSprites[d] = array(closed, closed, middle, middle, wide, wide, middle, middle);
        }
    }

    public Rectangle2D[] pacMunchingSprites(Direction dir) {
        return pacMunchingSprites[DIR_ORDER.index(dir)];
    }

    private final Rectangle2D[] pacDyingSprites = new Rectangle2D[11];

    {
        // TODO why do I get drawing artifacts if size is exactly 16?
        double size = 15.5;
        for (int i = 0; i < pacDyingSprites.length; ++i) {
            // TODO ensure last image is completely visible. What a mess!
            int y = i == 10 ? 1 : 0;
            pacDyingSprites[i] = rect(504 + i * 16, y, size - 0.5, size - 0.5);
        }
    }

    public Rectangle2D[] pacDyingSprites() {
        return pacDyingSprites;
    }

    private final Rectangle2D[][][] ghostNormalSprites = new Rectangle2D[4][4][];

    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostNormalSprites[id][d] = tilesRightOf(OFF_X, 2 * d, 4 + id, 2);
            }
        }
    }

    public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
        return ghostNormalSprites[id][DIR_ORDER.index(dir)];
    }

    private final Rectangle2D[] ghostFrightenedSprites = array(
        rect(OFF_X + r(8), r(4), r(1), r(1)),
        rect(OFF_X + r(9), r(4), r(1), r(1)));

    public Rectangle2D[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    private final Rectangle2D[] ghostFlashingSprites = tilesRightOf(OFF_X, 8, 4, 4);

    public Rectangle2D[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    private final Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];

    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = array(rect(OFF_X + r(8 + d), r(5), r(1), r(1)));
        }
    }

    public Rectangle2D[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[DIR_ORDER.index(dir)];
    }

    // Pac-Man specific:

    private final Rectangle2D[] bigPacManSprites = array(
        rect(OFF_X + 32, 16, 32, 32),
        rect(OFF_X + 64, 16, 32, 32),
        rect(OFF_X + 96, 16, 32, 32));

    public Rectangle2D[] bigPacManSprites() {
        return bigPacManSprites;
    }

    private final Rectangle2D[] blinkyDamagedSprites = array(
        rect(OFF_X + r(8), r(7), r(1), r(1)),
        rect(OFF_X + r(9), r(7), r(1), r(1)));

    public Rectangle2D[] blinkyDamagedSprites() {
        return blinkyDamagedSprites;
    }

    private final Rectangle2D[] blinkyStretchedSprites = tilesRightOf(OFF_X, 8, 6, 5);

    public Rectangle2D[] blinkyStretchedSprites() {
        return blinkyStretchedSprites;
    }

    private final Rectangle2D[] blinkyPatchedSprites = tilesRightOf(OFF_X, 10, 7, 2);

    public Rectangle2D[] blinkyPatchedSprites() {
        return blinkyPatchedSprites;
    }

    private final Rectangle2D[] blinkyNakedSprites = array(
        rect(OFF_X + r(8), r(8), r(2), r(1)),
        rect(OFF_X + r(10), r(8), r(2), r(1)));

    public Rectangle2D[] blinkyNakedSprites() {
        return blinkyNakedSprites;
    }
}