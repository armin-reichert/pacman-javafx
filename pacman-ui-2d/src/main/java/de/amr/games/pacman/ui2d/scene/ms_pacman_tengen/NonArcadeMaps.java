/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.image.Image;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.RectArea.rect;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;

public class NonArcadeMaps {

    // Strange map #15 (level 32) has 3 different images to create an animation effect
    // Image file "non_arcade_mazes.png"
    static final RectArea[] STRANGE_MAP_15_SPRITES = {
            rect(1568,  840, 224, 248),
            rect(1568, 1088, 224, 248),
            rect(1568, 1336, 224, 248),
    };

    // Strange map row counts as they appear in the sprite sheet
    static final byte[] STRANGE_MAPS_ROW_COUNTS = {
            31, 31, 31, 31, 31, 31, 30, 31,
            31, 37, 31, 31, 31, 37, 31, 25,
            37, 31, 37, 37, 37, 37, 37, 31,
            37, 37, 31, 25, 31, 25, 31, 31, 37,
            25, 25, 25, 25,
    };

    private final Image image;

    public NonArcadeMaps(AssetStorage assets) {
        image = assets.image(GameAssets2D.PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade");
    }

    private ImageAreaWithColorScheme sprite(int spriteNumber, NES_ColorScheme colorScheme) {
        int columnIndex, y;
        switch (spriteNumber) {
            case 1,2,3,4,5,6,7,8            -> { columnIndex = (spriteNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { columnIndex = (spriteNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { columnIndex = (spriteNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { columnIndex = (spriteNumber - 25); y = 840;  }
            case 34,35,36,37                -> { columnIndex = (spriteNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + spriteNumber);
        }
        int width = 28 * TS, height = STRANGE_MAPS_ROW_COUNTS[spriteNumber - 1] * TS;
        RectArea area = new RectArea(columnIndex * width, y, width, height);
        return new ImageAreaWithColorScheme(image, area, colorScheme);
    }

    // Creates pattern (00000000 11111111 22222222 11111111)...
    public RectArea strangeMap15Sprite(long tick) {
        int numFrames = 4, frameDuration = 8;
        int index = (int) ((tick % (numFrames * frameDuration)) / frameDuration);
        // (0, 1, 2, 3) -> (0, 1, 2, 1)
        return STRANGE_MAP_15_SPRITES[index == 3 ? 1 : index];
    }

    public ImageAreaWithColorScheme miniMapSprite(int mapNumber, NES_ColorScheme colorScheme) {
        int spriteNumber = switch (mapNumber) {
            case 1 -> 34;
            case 2 -> 35;
            case 3 -> 36;
            case 4 -> 30;
            case 5 -> 28;
            case 6 -> 37;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };
        NES_ColorScheme colorSchemeInSheet = switch (mapNumber) {
            case 1 -> _36_15_20_PINK_RED_WHITE;
            case 2 -> _21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> _35_28_20_PINK_YELLOW_WHITE;
            case 4 -> _28_16_20_YELLOW_RED_WHITE;
            case 5 -> _00_2A_24_GRAY_GREEN_PINK;
            case 6 -> _23_20_2B_VIOLET_WHITE_GREEN;
            default -> null;
        };
        return sprite(spriteNumber, colorScheme.equals(colorSchemeInSheet) ? colorScheme : null);
    }

    public ImageAreaWithColorScheme bigMapSprite(int mapNumber, NES_ColorScheme colorScheme) {
        int spriteNumber = switch (mapNumber) {
            case  1 -> 19;
            case  2 -> 20;
            case  3 -> 21;
            case  4 -> 22;
            case  5 -> 23;
            case  6 -> 17;
            case  7 -> 10;
            case  8 -> 14;
            case  9 -> 26;
            case 10 -> 25;
            case 11 -> 33;
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + mapNumber);
        };
        NES_ColorScheme colorSchemeInSheet = switch (mapNumber) {
            case  1 -> _07_20_20_BROWN_WHITE_WHITE;
            case  2 -> _15_25_20_RED_ROSE_WHITE;
            case  3 -> _0F_20_1C_BLACK_WHITE_GREEN;
            case  4 -> _19_20_20_GREEN_WHITE_WHITE;
            case  5 -> _0C_20_14_GREEN_WHITE_VIOLET;
            case  6 -> _25_20_20_ROSE_WHITE_WHITE;
            case  7 -> _0F_01_20_BLACK_BLUE_WHITE;
            case  8 -> _28_20_2A_YELLOW_WHITE_GREEN;
            case  9 -> _03_20_20_VIOLET_WHITE_WHITE;
            case 10 -> _10_20_28_GRAY_WHITE_YELLOW;
            case 11 -> _15_25_20_RED_ROSE_WHITE;
            default -> null;
        };
        return sprite(spriteNumber, colorScheme.equals(colorSchemeInSheet) ? colorScheme : null);
    }

    public ImageAreaWithColorScheme strangeMapSprite(int levelNumber) {
        return sprite(levelNumber, null); // TODO
    }
}