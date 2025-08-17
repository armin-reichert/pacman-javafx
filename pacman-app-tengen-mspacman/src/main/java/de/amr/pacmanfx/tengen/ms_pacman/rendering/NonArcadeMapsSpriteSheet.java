/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.RectShort.rect;

public record NonArcadeMapsSpriteSheet(Image sourceImage) implements SpriteSheet<NonArcadeMapsSpriteSheet.MazeID> {

    // MazeIDs they appear in the sprite sheet (row by row)
    public enum MazeID {
        MAZE1, MAZE2, MAZE3, MAZE4, MAZE5, MAZE6, MAZE7, MAZE8,
        MAZE9, MAZE10_BIG, MAZE11, MAZE12, MAZE13, MAZE14_BIG, MAZE15, MAZE16_MINI,
        MAZE17_BIG, MAZE18, MAZE19_BIG, MAZE20_BIG, MAZE21_BIG, MAZE22_BIG, MAZE23_BIG, MAZE24,
        MAZE25_BIG, MAZE26_BIG, MAZE27, MAZE28_MINI, MAZE29, MAZE30_MINI, MAZE31, MAZE32_ANIMATED, MAZE33_BIG,
        MAZE34_MINI, MAZE35_MINI, MAZE36_MINI, MAZE37_MINI
    }

    // Height (in tiles) of the maps as they appear in the sprite sheet (row by row)
    private static final byte[][] MAP_HEIGHT_IN_TILES = {
        {31, 31, 31, 31, 31, 31, 30, 31},
        {31, 37, 31, 31, 31, 37, 31, 25},
        {37, 31, 37, 37, 37, 37, 37, 31},
        {37, 37, 31, 25, 31, 25, 31, 31 /* 3 frames */, 37 /* BIG map */},
        {25, 25, 25, 25} // 4 MINI maps
    };

    private static final SpriteMap<MazeID> SPRITE_MAP = new SpriteMap<>(MazeID.class);
    static {
        // Example: MazeID.MAZE10 = ids[9]
        MazeID[] ids = MazeID.values();
        int idNumber = 1;

        int width = 28 * TS;
        // y position of maze upper edges by row index
        int[] ys = {0, 248, 544, 840, 1136};

        // first 4 rows
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 8; ++col) {
                int height = MAP_HEIGHT_IN_TILES[row][col] * TS;
                if (idNumber != 32) {
                    SPRITE_MAP.add(ids[idNumber - 1], rect(col * width, ys[row], width, height));
                    ++idNumber;
                }
            }
        }

        // Maze #32 (last in STRANGE level) is special and has 3 animation frames
        SPRITE_MAP.add(MazeID.MAZE32_ANIMATED, rect(1568, 840, 224, 248), rect(1568, 1088, 224, 248), rect(1568, 1336, 224, 248));

        // row 3, col 8:  Maze #33 is BIG map
        SPRITE_MAP.add(MazeID.MAZE33_BIG, rect(8 * width, ys[3], width, MAP_HEIGHT_IN_TILES[3][8] * TS));

        // last row: 4 MINI mazes
        int lastRow = 4, y = ys[lastRow];
        SPRITE_MAP.add(MazeID.MAZE34_MINI, rect(    0,     y, width, MAP_HEIGHT_IN_TILES[lastRow][0] * TS));
        SPRITE_MAP.add(MazeID.MAZE35_MINI, rect(    width, y, width, MAP_HEIGHT_IN_TILES[lastRow][1] * TS));
        SPRITE_MAP.add(MazeID.MAZE36_MINI, rect(2 * width, y, width, MAP_HEIGHT_IN_TILES[lastRow][2] * TS));
        SPRITE_MAP.add(MazeID.MAZE37_MINI, rect(3 * width, y, width, MAP_HEIGHT_IN_TILES[lastRow][3] * TS));
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
