/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.RectShort.rect;

/**
 * SpriteSheet for non‐arcade maps in Tengen Ms. Pac-Man.
 *
 * <p>Each MazeID corresponds to a sub‐image region laid out row by row
 * in the source image. Some mazes (32, 33, 34–37) are handled specially.
 */
public final class NonArcadeMapsSpriteSheet implements SpriteSheet<NonArcadeMapsSpriteSheet.MapID> {

    public static final NonArcadeMapsSpriteSheet INSTANCE = new NonArcadeMapsSpriteSheet();

    // Map IDs as they appear in the sprite sheet (row by row)
    public enum MapID {
        MAP1, MAP2, MAP3, MAP4, MAP5, MAP6, MAP7, MAP8,
        MAP9, MAP10_BIG, MAP11, MAP12, MAP13, MAP14_BIG, MAP15, MAP16_MINI,
        MAP17_BIG, MAP18, MAP19_BIG, MAP20_BIG, MAP21_BIG, MAP22_BIG, MAP23_BIG, MAP24,
        MAP25_BIG, MAP26_BIG, MAP27, MAP28_MINI, MAP29, MAP30_MINI, MAP31, MAP32_ANIMATED, MAP33_BIG,
        MAP34_MINI, MAP35_MINI, MAP36_MINI, MAP37_MINI
    }

    private static final SpriteMap<MapID> SPRITE_MAP = new SpriteMap<>(MapID.class);

    static {
        // Example: MazeID.MAP10 = MAP_IDS[9]
        final MapID[] MAP_IDS = MapID.values();

        final int MAP_WIDTH = 28 * TS;

        // Height of the mazes as they appear in the sprite sheet (row by row)
        final int[][] MAP_HEIGHTS = {
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
                    MapID id = MAP_IDS[num - 1];
                    SPRITE_MAP.add(id, rect(col * MAP_WIDTH, Y_POS[row], MAP_WIDTH, MAP_HEIGHTS[row][col]));
                }
                ++num;
            }
        }

        // Maze #32 (last in STRANGE level) has 3 animation frames
        SPRITE_MAP.add(MapID.MAP32_ANIMATED,
            rect(1568,  840, MAP_WIDTH, 31 * TS),
            rect(1568, 1088, MAP_WIDTH, 31 * TS),
            rect(1568, 1336, MAP_WIDTH, 31 * TS));

        // row=3, col=8:  Maze #33 is BIG
        SPRITE_MAP.add(MapID.MAP33_BIG, rect(8 * MAP_WIDTH, Y_POS[3], MAP_WIDTH, MAP_HEIGHTS[3][8]));

        // row=4: 4 MINI mazes
        SPRITE_MAP.add(MapID.MAP34_MINI, rect(             0, Y_POS[4], MAP_WIDTH, MAP_HEIGHTS[4][0]));
        SPRITE_MAP.add(MapID.MAP35_MINI, rect(    MAP_WIDTH, Y_POS[4], MAP_WIDTH, MAP_HEIGHTS[4][1]));
        SPRITE_MAP.add(MapID.MAP36_MINI, rect(2 * MAP_WIDTH, Y_POS[4], MAP_WIDTH, MAP_HEIGHTS[4][2]));
        SPRITE_MAP.add(MapID.MAP37_MINI, rect(3 * MAP_WIDTH, Y_POS[4], MAP_WIDTH, MAP_HEIGHTS[4][3]));

        SPRITE_MAP.checkCompleteness();
    }

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_UIConfig.class;

    private static final Image IMAGE = LOCAL_RESOURCES.loadImage(TengenMsPacMan_UIConfig.NON_ARCADE_MAPS_IMAGE_PATH);

    private NonArcadeMapsSpriteSheet() {}

    @Override
    public Image sourceImage() {
        return IMAGE;
    }

    @Override
    public RectShort sprite(MapID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] sprites(MapID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}
