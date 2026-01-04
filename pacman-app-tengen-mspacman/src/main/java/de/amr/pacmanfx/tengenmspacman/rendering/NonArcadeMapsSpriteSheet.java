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
 * <p>Each map ID corresponds to a sub‐image region laid out row by row
 * in the source image. Some mazes (32, 33, 34–37) are handled specially.
 */
public final class NonArcadeMapsSpriteSheet implements SpriteSheet<NonArcadeMapsSpriteSheet.MapID> {

    // Map IDs as they appear in the sprite sheet (row by row)
    public enum MapID {
        MAP1, MAP2, MAP3, MAP4, MAP5, MAP6, MAP7, MAP8,
        MAP9, MAP10_BIG, MAP11, MAP12, MAP13, MAP14_BIG, MAP15, MAP16_MINI,
        MAP17_BIG, MAP18, MAP19_BIG, MAP20_BIG, MAP21_BIG, MAP22_BIG, MAP23_BIG, MAP24,
        MAP25_BIG, MAP26_BIG, MAP27, MAP28_MINI, MAP29, MAP30_MINI, MAP31, MAP32_ANIMATED, MAP33_BIG,
        MAP34_MINI, MAP35_MINI, MAP36_MINI, MAP37_MINI
    }

    public static final NonArcadeMapsSpriteSheet INSTANCE = new NonArcadeMapsSpriteSheet();

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_UIConfig.class;

    private final Image image = LOCAL_RESOURCES.loadImage(TengenMsPacMan_UIConfig.NON_ARCADE_MAPS_IMAGE_PATH);
    private final SpriteMap<MapID> spriteMap = new SpriteMap<>(MapID.class);

    private NonArcadeMapsSpriteSheet() {
        final MapID[] ids = MapID.values();

        // All maps are 28 tiles wide
        final int mapWidth = 28 * TS;

        // Map height values as they appear in the sprite sheet (row by row)
        final int[][] mapHeights = {
            {31 * TS, 31 * TS, 31 * TS, 31 * TS, 31 * TS, 31 * TS, 30 * TS, 31 * TS},
            {31 * TS, 37 * TS, 31 * TS, 31 * TS, 31 * TS, 37 * TS, 31 * TS, 25 * TS},
            {37 * TS, 31 * TS, 37 * TS, 37 * TS, 37 * TS, 37 * TS, 37 * TS, 31 * TS},
            {37 * TS, 37 * TS, 31 * TS, 25 * TS, 31 * TS, 25 * TS, 31 * TS, 31 * TS, 37 * TS},
            {25 * TS, 25 * TS, 25 * TS, 25 * TS}
        };

        // y position of maze upper edges by row index
        final int[] yPos = {0, 248, 544, 840, 1136};

        int number = 1;
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 8; ++col) {
                if (number != 32) {
                    final MapID id = ids[number - 1];
                    spriteMap.add(id, rect(col * mapWidth, yPos[row], mapWidth, mapHeights[row][col]));
                }
                ++number;
            }
        }

        // Maze #32 (last in STRANGE level) has 3 animation frames
        spriteMap.add(MapID.MAP32_ANIMATED,
            rect(1568,  840, mapWidth, 31 * TS),
            rect(1568, 1088, mapWidth, 31 * TS),
            rect(1568, 1336, mapWidth, 31 * TS));

        // row=3, col=8:  Maze #33 is BIG
        spriteMap.add(MapID.MAP33_BIG, rect(8 * mapWidth, yPos[3], mapWidth, mapHeights[3][8]));

        // row=4: 4 MINI mazes
        spriteMap.add(MapID.MAP34_MINI, rect(           0, yPos[4], mapWidth, mapHeights[4][0]));
        spriteMap.add(MapID.MAP35_MINI, rect(    mapWidth, yPos[4], mapWidth, mapHeights[4][1]));
        spriteMap.add(MapID.MAP36_MINI, rect(2 * mapWidth, yPos[4], mapWidth, mapHeights[4][2]));
        spriteMap.add(MapID.MAP37_MINI, rect(3 * mapWidth, yPos[4], mapWidth, mapHeights[4][3]));

        spriteMap.checkCompleteness();
    }

    @Override
    public Image sourceImage() {
        return image;
    }

    @Override
    public RectShort sprite(MapID id) {
        return spriteMap.sprite(id);
    }

    @Override
    public RectShort[] sprites(MapID id) {
        return spriteMap.spriteSequence(id);
    }
}
