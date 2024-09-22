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

    private static final RectangularArea FULL_MAZE_SPRITE = rect(0, 0, 224, 248);
    private static final RectangularArea EMPTY_MAZE_SPRITE = rect(228, 0, 224, 248);

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

    private static final RectangularArea[] GHOST_NUMBER_SPRITES = rectArray(
        rect(456, 133, 15, 7),  // 200
        rect(472, 133, 15, 7),  // 400
        rect(488, 133, 15, 7),  // 800
        rect(504, 133, 16, 7)); // 1600

    private static final RectangularArea[] BONUS_SYMBOL_SPRITES = IntStream.range(0, 8)
        .mapToObj(i -> rect(OFF_X + RASTER_SIZE * (2 + i), 49,  14, 14))
        .toArray(RectangularArea[]::new);

    private static final RectangularArea[] BONUS_VALUE_SPRITES = {
        rect(457, 148, 14, 7), //  100
        rect(472, 148, 15, 7), //  300
        rect(488, 148, 15, 7), //  500
        rect(504, 148, 15, 7), //  700
        rect(520, 148, 18, 7), // 1000
        rect(518, 164, 20, 7), // 2000
        rect(518, 180, 20, 7), // 3000
        rect(518, 196, 20, 7), // 5000
    };

    private static final RectangularArea[] GHOST_FACING_RIGHT_SPRITES = new RectangularArea[4];
    static {
        for (byte id = 0; id < 4; ++id) {
            int rx = 2 * ORDER.indexOf(Direction.RIGHT);
            int ry = 4 + id;
            GHOST_FACING_RIGHT_SPRITES[id] = rect(OFF_X + RASTER_SIZE * (rx), RASTER_SIZE * (ry), RASTER_SIZE, RASTER_SIZE);
        }
    }

    private static final RectangularArea LIVES_COUNTER_SPRITE = rect(OFF_X + 129, 15, 16, 16);

    private static final RectangularArea[][] PAC_MUNCHING_SPRITES = new RectangularArea[4][];
    static {
        byte margin = 1;
        int size = RASTER_SIZE - 2 * margin;
        for (byte d = 0; d < 4; ++d) {
            var wide   = rect(OFF_X      + margin, d * 16 + margin, size, size);
            var middle = rect(OFF_X + 16 + margin, d * 16 + margin, size, size);
            var closed = rect(OFF_X + 32 + margin,          margin, size, size);
            PAC_MUNCHING_SPRITES[d] = rectArray(closed, closed, middle, middle, wide, wide, middle, middle);
        }
    }

    private static final RectangularArea[] PAC_DYING_SPRITES = new RectangularArea[11];
    static {
        for (int i = 0; i < PAC_DYING_SPRITES.length; ++i) {
            boolean last = i == PAC_DYING_SPRITES.length - 1;
            PAC_DYING_SPRITES[i] = rect(504 + i * 16, 0, 15, last ? 16 : 15);
        }
    }

    private static final RectangularArea[][][] GHOST_NORMAL_SPRITES = new RectangularArea[4][4][];
    static {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                GHOST_NORMAL_SPRITES[id][d] = tilesRightOf(2 * d, 4 + id, 2);
            }
        }
    }

    private static final RectangularArea[] GHOST_FRIGHTENED_SPRITES = rectArray(
        rect(OFF_X + RASTER_SIZE * (8), RASTER_SIZE * (4), RASTER_SIZE, RASTER_SIZE),
        rect(OFF_X + RASTER_SIZE * (9), RASTER_SIZE * (4), RASTER_SIZE, RASTER_SIZE));

    private static final RectangularArea[] GHOST_FLASHING_SPRITES = tilesRightOf(8, 4, 4);

    private static final RectangularArea[][] GHOST_EYES_SPRITES = new RectangularArea[4][];
    static {
        for (byte d = 0; d < 4; ++d) {
            GHOST_EYES_SPRITES[d] = rectArray(rect(OFF_X + RASTER_SIZE * (8 + d), RASTER_SIZE * (5), RASTER_SIZE, RASTER_SIZE));
        }
    }

    private static final RectangularArea[] BIG_PAC_MAN_SPRITES = rectArray(
        rect(OFF_X + 32, 16, 32, 32), rect(OFF_X + 64, 16, 32, 32), rect(OFF_X + 96, 16, 32, 32));

    private static final RectangularArea[] BLINKY_STRETCHED_SPRITES = new RectangularArea[5];
    static {
        for (int i = 0; i < 5; ++i) {
            BLINKY_STRETCHED_SPRITES[i] = rect(OFF_X + RASTER_SIZE * (8 + i), RASTER_SIZE * (6), RASTER_SIZE, RASTER_SIZE);
        }
    }

    private static final RectangularArea[] BLINKY_DAMAGED_SPRITES = new RectangularArea[2];
    static {
        byte margin = 1;
        int size = RASTER_SIZE - 2 * margin;
        BLINKY_DAMAGED_SPRITES[0] = rect(OFF_X + RASTER_SIZE * (8) + margin, RASTER_SIZE * (7) + margin, size, size);
        BLINKY_DAMAGED_SPRITES[1] = rect(OFF_X + RASTER_SIZE * (9) + margin, RASTER_SIZE * (7) + margin, size, size);
    }

    private static final RectangularArea[] BLINKY_PATCHED_SPRITES = new RectangularArea[2];
    static {
        BLINKY_PATCHED_SPRITES[0] = rect(OFF_X + RASTER_SIZE * (10), RASTER_SIZE * (7), RASTER_SIZE, RASTER_SIZE);
        BLINKY_PATCHED_SPRITES[1] = rect(OFF_X + RASTER_SIZE * (11), RASTER_SIZE * (7), RASTER_SIZE, RASTER_SIZE);
    }

    private static final RectangularArea[] BLINKY_NAKED_SPRITES = new RectangularArea[2];
    static {
        BLINKY_NAKED_SPRITES[0] = rect(OFF_X + RASTER_SIZE * (8), RASTER_SIZE * (8), 2 * RASTER_SIZE, RASTER_SIZE);
        BLINKY_NAKED_SPRITES[1] = rect(OFF_X + RASTER_SIZE * (10), RASTER_SIZE * (8), 2 * RASTER_SIZE, RASTER_SIZE);
    }

    private final Image source;

    public PacManGameSpriteSheet(Image source) {
        this.source = source;
    }

    RectangularArea getFullMazeSprite() {
        return FULL_MAZE_SPRITE;
    }
    RectangularArea getEmptyMazeSprite() {
        return EMPTY_MAZE_SPRITE;
    }

    @Override
    public Image sourceImage() {
        return source;
    }

    @Override
    public RectangularArea[] ghostNumberSprites() {
        return GHOST_NUMBER_SPRITES;
    }

    @Override
    public RectangularArea bonusSymbolSprite(byte symbol) {
        return BONUS_SYMBOL_SPRITES[symbol];
    }

    @Override
    public RectangularArea bonusValueSprite(byte symbol) {
        return BONUS_VALUE_SPRITES[symbol];
    }

    @Override
    public RectangularArea livesCounterSprite() {
        return LIVES_COUNTER_SPRITE;
    }

    public RectangularArea[] pacMunchingSprites(Direction dir) {
        return PAC_MUNCHING_SPRITES[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] pacDyingSprites() {
        return PAC_DYING_SPRITES;
    }

    @Override
    public RectangularArea[] ghostNormalSprites(byte id, Direction dir) {
        return GHOST_NORMAL_SPRITES[id][ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] ghostFrightenedSprites() {
        return GHOST_FRIGHTENED_SPRITES;
    }

    @Override
    public RectangularArea[] ghostFlashingSprites() {
        return GHOST_FLASHING_SPRITES;
    }

    @Override
    public RectangularArea[] ghostEyesSprites(Direction dir) {
        return GHOST_EYES_SPRITES[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea ghostFacingRight(byte ghostID) {
        return GHOST_FACING_RIGHT_SPRITES[ghostID];
    }

    @Override
    public RectangularArea[] bigPacManSprites() {
        return BIG_PAC_MAN_SPRITES;
    }

    @Override
    public RectangularArea[] blinkyStretchedSprites() {
        return BLINKY_STRETCHED_SPRITES;
    }

    @Override
    public RectangularArea[] blinkyDamagedSprites() {
        return BLINKY_DAMAGED_SPRITES;
    }

    @Override
    public RectangularArea[] blinkyPatchedSprites() {
        return BLINKY_PATCHED_SPRITES;
    }

    @Override
    public RectangularArea[] blinkyNakedSprites() {
        return BLINKY_NAKED_SPRITES;
    }
}