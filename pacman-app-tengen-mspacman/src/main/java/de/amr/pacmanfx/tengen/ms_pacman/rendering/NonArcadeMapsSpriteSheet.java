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

    public enum MazeID {
        MAZE1, MAZE2, MAZE3, MAZE4, MAZE5, MAZE6, MAZE7, MAZE8, MAZE9, MAZE10,
        MAZE11, MAZE12, MAZE13, MAZE14, MAZE15, MAZE16, MAZE17, MAZE18, MAZE19,
        MAZE20, MAZE21, MAZE22, MAZE23, MAZE24, MAZE25, MAZE26, MAZE27, MAZE28,
        MAZE29, MAZE30, MAZE31, MAZE32, MAZE33, MAZE34, MAZE35, MAZE36
    }

    private static final SpriteMap<MazeID> SPRITE_MAP = new SpriteMap<>(MazeID.class);

    // Map row counts as they appear in the non-ARCADE mazes sprite sheet (row by row)
    private static final byte[] NON_ARCADE_MAP_ROW_COUNTS = {
        31, 31, 31, 31, 31, 31, 30, 31,
        31, 37, 31, 31, 31, 37, 31, 25,
        37, 31, 37, 37, 37, 37, 37, 31,
        37, 37, 31, 25, 31, 25, 31, 31, 37,
        25, 25, 25, 25,
    };

    static {
        MazeID[] ids = MazeID.values();
        int idNumber = 1;

        int width = 28 * TS;
        int[] ys = {0, 248, 544, 840, 1136};

        // first 4 rows
        for (int row = 0; row < 5; ++row) {
            for (int col = 0; col < 8; ++col) {
                int height = NON_ARCADE_MAP_ROW_COUNTS[col] * TS;
                if (idNumber != 32) {
                    SPRITE_MAP.add(ids[idNumber - 1], new RectShort(col * width, ys[row], width, height));
                    ++idNumber;
                }
                if (row == 4 && col == 3) {
                    break; // last maze in sprite sheet reached
                }
            }
        }
        // Maze #32 is special and has 3 animation frames
        SPRITE_MAP.add(MazeID.MAZE32,
            rect(1568, 840, 224, 248), rect(1568, 1088, 224, 248), rect(1568, 1336, 224, 248)
        );
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
