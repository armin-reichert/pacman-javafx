/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.RectShort.rect;
import static java.util.Objects.requireNonNull;

/**
 * SpriteSheet for non‐arcade maps in Tengen Ms. Pac-Man.
 *
 * <p>Each MazeID corresponds to a sub‐image region laid out row by row
 * in the source image. Some mazes (32, 33, 34–37) are handled specially.
 */
public record NonArcadeMapsSpriteSheet(Image sourceImage) implements SpriteSheet<NonArcadeMapsSpriteSheet.MazeID> {

    // MazeIDs as they appear in the sprite sheet (row by row)
    public enum MazeID {
        MAZE1, MAZE2, MAZE3, MAZE4, MAZE5, MAZE6, MAZE7, MAZE8,
        MAZE9, MAZE10_BIG, MAZE11, MAZE12, MAZE13, MAZE14_BIG, MAZE15, MAZE16_MINI,
        MAZE17_BIG, MAZE18, MAZE19_BIG, MAZE20_BIG, MAZE21_BIG, MAZE22_BIG, MAZE23_BIG, MAZE24,
        MAZE25_BIG, MAZE26_BIG, MAZE27, MAZE28_MINI, MAZE29, MAZE30_MINI, MAZE31, MAZE32_ANIMATED, MAZE33_BIG,
        MAZE34_MINI, MAZE35_MINI, MAZE36_MINI, MAZE37_MINI
    }

    private static final SpriteMap<MazeID> SPRITE_MAP = new SpriteMap<>(MazeID.class);

    static {
        // Example: MazeID.MAZE10 = MAZE_IDS[9]
        final MazeID[] MAZE_IDS = MazeID.values();

        final int MAZE_WIDTH = 28 * TS;

        // Height of the mazes as they appear in the sprite sheet (row by row)
        final int[][] MAZE_HEIGHTS = {
            {31 * TS, 31 * TS, 31 * TS, 31 * TS, 31 * TS, 31 * TS, 30 * TS, 31 * TS},
            {31 * TS, 37 * TS, 31 * TS, 31 * TS, 31 * TS, 37 * TS, 31 * TS, 25 * TS},
            {37 * TS, 31 * TS, 37 * TS, 37 * TS, 37 * TS, 37 * TS, 37 * TS, 31 * TS},
            {37 * TS, 37 * TS, 31 * TS, 25 * TS, 31 * TS, 25 * TS, 31 * TS, 31 * TS, 37 * TS},
            {25 * TS, 25 * TS, 25 * TS, 25 * TS}
        };

        // y position of maze upper edges by row index
        final int[] Y_POS = {0, 248, 544, 840, 1136};

        int num = 1;
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 8; ++col) {
                if (num != 32) {
                    MazeID id = MAZE_IDS[num - 1];
                    SPRITE_MAP.add(id, rect(col * MAZE_WIDTH, Y_POS[row], MAZE_WIDTH, MAZE_HEIGHTS[row][col]));
                }
                ++num;
            }
        }

        // Maze #32 (last in STRANGE level) has 3 animation frames
        SPRITE_MAP.add(MazeID.MAZE32_ANIMATED,
            rect(1568,  840, MAZE_WIDTH, 31 * TS),
            rect(1568, 1088, MAZE_WIDTH, 31 * TS),
            rect(1568, 1336, MAZE_WIDTH, 31 * TS));

        // row=3, col=8:  Maze #33 is BIG
        SPRITE_MAP.add(MazeID.MAZE33_BIG, rect(8 * MAZE_WIDTH, Y_POS[3], MAZE_WIDTH, MAZE_HEIGHTS[3][8]));

        // row=4: 4 MINI mazes
        SPRITE_MAP.add(MazeID.MAZE34_MINI, rect(             0, Y_POS[4], MAZE_WIDTH, MAZE_HEIGHTS[4][0]));
        SPRITE_MAP.add(MazeID.MAZE35_MINI, rect(    MAZE_WIDTH, Y_POS[4], MAZE_WIDTH, MAZE_HEIGHTS[4][1]));
        SPRITE_MAP.add(MazeID.MAZE36_MINI, rect(2 * MAZE_WIDTH, Y_POS[4], MAZE_WIDTH, MAZE_HEIGHTS[4][2]));
        SPRITE_MAP.add(MazeID.MAZE37_MINI, rect(3 * MAZE_WIDTH, Y_POS[4], MAZE_WIDTH, MAZE_HEIGHTS[4][3]));

        SPRITE_MAP.checkCompleteness();
    }

    public NonArcadeMapsSpriteSheet {
        requireNonNull(sourceImage, "Sprite sheet source image must not be null");
    }

    @Override
    public RectShort sprite(MazeID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] spriteSequence(MazeID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}
