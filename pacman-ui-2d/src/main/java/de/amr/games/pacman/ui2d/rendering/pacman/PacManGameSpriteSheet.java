/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.rendering.SpriteSheet;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.games.pacman.ui2d.rendering.RectangularArea.rect;
import static de.amr.games.pacman.ui2d.rendering.SpriteSheet.rectArray;

/**
 * @author Armin Reichert
 */
public class PacManGameSpriteSheet implements SpriteSheet {

    private static final int RASTER_SIZE = 16;
    private static final int OFF_X = 456;

    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    private static final RectangularArea fullMazeSprite = rect(0, 0, 224, 248);
    private static final RectangularArea emptyMazeSprite = rect(228, 0, 224, 248);

    /**
     * @param tileX    grid column (in tile coordinates)
     * @param tileY    grid row (in tile coordinates)
     * @param numTiles number of tiles
     * @return horizontal stripe of tiles at given grid position
     */
    private static RectangularArea[] tilesRightOf(int tileX, int tileY, int numTiles) {
        var tiles = new RectangularArea[numTiles];
        for (int i = 0; i < numTiles; ++i) {
            tiles[i] = rect(OFF_X + RASTER_SIZE * (tileX + i), RASTER_SIZE * tileY, RASTER_SIZE, RASTER_SIZE);
        }
        return tiles;
    }


    private static final RectangularArea[] ghostNumberSprites = rectArray(
        rect(456, 133, 15, 7),  // 200
        rect(472, 133, 15, 7),  // 400
        rect(488, 133, 15, 7),  // 800
        rect(504, 133, 16, 7)); // 1600

    private static final RectangularArea[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(i -> rect(OFF_X + RASTER_SIZE * (2 + i), RASTER_SIZE * 3, RASTER_SIZE, RASTER_SIZE))
        .toArray(RectangularArea[]::new);

    private static final RectangularArea[] bonusValueSprites = {
        rect(457, 148, 14, 7), //  100
        rect(472, 148, 15, 7), //  300
        rect(488, 148, 15, 7), //  500
        rect(504, 148, 15, 7), //  700
        rect(520, 148, 18, 7), // 1000
        rect(518, 164, 20, 7), // 2000
        rect(518, 180, 20, 7), // 3000
        rect(518, 196, 20, 7), // 5000
    };

    private static final RectangularArea[] ghostFacingRightSprites = new RectangularArea[4];
    static {
        for (byte id = 0; id < 4; ++id) {
            int rx = 2 * ORDER.indexOf(Direction.RIGHT);
            int ry = 4 + id;
            ghostFacingRightSprites[id] = rect(OFF_X + RASTER_SIZE * (rx), RASTER_SIZE * (ry), RASTER_SIZE, RASTER_SIZE);
        }
    }

    private static final RectangularArea livesCounterSprite = rect(OFF_X + 129, 15, 16, 16);

    private static final RectangularArea[][] pacMunchingSprites = new RectangularArea[4][];
    static {
        byte margin = 1;
        int size = RASTER_SIZE - 2 * margin;
        for (byte d = 0; d < 4; ++d) {
            var wide   = rect(OFF_X      + margin, d * 16 + margin, size, size);
            var middle = rect(OFF_X + 16 + margin, d * 16 + margin, size, size);
            var closed = rect(OFF_X + 32 + margin,          margin, size, size);
            pacMunchingSprites[d] = rectArray(closed, closed, middle, middle, wide, wide, middle, middle);
        }
    }

    private static final RectangularArea[] pacDyingSprites = new RectangularArea[11];
    static {
        for (int i = 0; i < pacDyingSprites.length; ++i) {
            boolean last = i == pacDyingSprites.length - 1;
            pacDyingSprites[i] = rect(504 + i * 16, 0, 15, last ? 16 : 15);
        }
    }

    private static final RectangularArea[][][] ghostNormalSprites = new RectangularArea[4][4][];
    static {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostNormalSprites[id][d] = tilesRightOf(2 * d, 4 + id, 2);
            }
        }
    }

    private static final RectangularArea[] ghostFrightenedSprites = rectArray(
        rect(OFF_X + RASTER_SIZE * (8), RASTER_SIZE * (4), RASTER_SIZE, RASTER_SIZE),
        rect(OFF_X + RASTER_SIZE * (9), RASTER_SIZE * (4), RASTER_SIZE, RASTER_SIZE));

    private static final RectangularArea[] ghostFlashingSprites = tilesRightOf(8, 4, 4);

    private static final RectangularArea[][] ghostEyesSprites = new RectangularArea[4][];
    static {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = rectArray(rect(OFF_X + RASTER_SIZE * (8 + d), RASTER_SIZE * (5), RASTER_SIZE, RASTER_SIZE));
        }
    }

    private static final RectangularArea[] bigPacManSprites = rectArray(
        rect(OFF_X + 32, 16, 32, 32),
        rect(OFF_X + 64, 16, 32, 32),
        rect(OFF_X + 96, 16, 32, 32));

    private static final RectangularArea[] blinkyStretchedSprites = new RectangularArea[5];
    static {
        for (int i = 0; i < 5; ++i) {
            blinkyStretchedSprites[i] = rect(OFF_X + RASTER_SIZE * (8 + i), RASTER_SIZE * (6), RASTER_SIZE, RASTER_SIZE);
        }
    }

    private static final RectangularArea[] blinkyDamagedSprites = new RectangularArea[2];
    static {
        byte margin = 1;
        int size = RASTER_SIZE - 2 * margin;
        blinkyDamagedSprites[0] = rect(OFF_X + RASTER_SIZE * (8) + margin, RASTER_SIZE * (7) + margin, size, size);
        blinkyDamagedSprites[1] = rect(OFF_X + RASTER_SIZE * (9) + margin, RASTER_SIZE * (7) + margin, size, size);
    }

    private static final RectangularArea[] blinkyPatchedSprites = new RectangularArea[2];
    static {
        blinkyPatchedSprites[0] = rect(OFF_X + RASTER_SIZE * (10), RASTER_SIZE * (7), RASTER_SIZE, RASTER_SIZE);
        blinkyPatchedSprites[1] = rect(OFF_X + RASTER_SIZE * (11), RASTER_SIZE * (7), RASTER_SIZE, RASTER_SIZE);
    }

    private static final RectangularArea[] blinkyNakedSprites = new RectangularArea[2];
    static {
        blinkyNakedSprites[0] = rect(OFF_X + RASTER_SIZE * (8), RASTER_SIZE * (8), 2 * RASTER_SIZE, RASTER_SIZE);
        blinkyNakedSprites[1] = rect(OFF_X + RASTER_SIZE * (10), RASTER_SIZE * (8), 2 * RASTER_SIZE, RASTER_SIZE);
    }

    private final Image source;

    public PacManGameSpriteSheet(Image source) {
        this.source = source;
    }

    public RectangularArea getFullMazeSprite() {
        return fullMazeSprite;
    }
    public RectangularArea getEmptyMazeSprite() {
        return emptyMazeSprite;
    }
    public RectangularArea ghostFacingRight(byte ghostID) {
        return ghostFacingRightSprites[ghostID];
    }

    @Override
    public Image sourceImage() {
        return source;
    }

    @Override
    public RectangularArea[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    @Override
    public RectangularArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    @Override
    public RectangularArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    @Override
    public RectangularArea livesCounterSprite() {
        return livesCounterSprite;
    }

    public RectangularArea[] pacMunchingSprites(Direction dir) {
        return pacMunchingSprites[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] pacDyingSprites() {
        return pacDyingSprites;
    }

    @Override
    public RectangularArea[] ghostNormalSprites(byte id, Direction dir) {
        return ghostNormalSprites[id][ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    @Override
    public RectangularArea[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    @Override
    public RectangularArea[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] bigPacManSprites() {
        return bigPacManSprites;
    }

    @Override
    public RectangularArea[] blinkyStretchedSprites() {
        return blinkyStretchedSprites;
    }

    @Override
    public RectangularArea[] blinkyDamagedSprites() {
        return blinkyDamagedSprites;
    }

    @Override
    public RectangularArea[] blinkyPatchedSprites() {
        return blinkyPatchedSprites;
    }

    @Override
    public RectangularArea[] blinkyNakedSprites() {
        return blinkyNakedSprites;
    }
}