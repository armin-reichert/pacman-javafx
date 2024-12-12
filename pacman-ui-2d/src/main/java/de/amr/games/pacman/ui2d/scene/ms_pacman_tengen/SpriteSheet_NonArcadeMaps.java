/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.RectArea.rect;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;
import static de.amr.games.pacman.ui2d.util.Ufx.*;

/**
 * This class provides an API to access the images in file {@code non_arcade_mazes.png}.
 * <p>
 * This PNG file contains all mazes which are used for the MINI, BIG and STRANGE map categories.
 * The color schemes correspond to the ones used in STRANGE mode running through all levels (1-32).
 * Because levels 28-31 use random color schemes, and the MINI and BIG categories use their
 * map in varying color schemes, the images in the file do not cover all required map/color scheme
 * combinations.
 * </p>
 * <p>
*  For this reason, a cache is provided where each maze image contained in the file can be
 * stored after having been recolored to the color scheme required in the game level.
 * </p>
 */
public class SpriteSheet_NonArcadeMaps {

    private record MazeSpec(MapCategory mapCategory, int spriteNumber, NES_ColorScheme colorScheme) {}

    // Strange map #15 (level 32) has 3 different images to create an animation effect
    static final RectArea[] STRANGE_MAP_15_SPRITES = {
        rect(1568,  840, 224, 248),
        rect(1568, 1088, 224, 248),
        rect(1568, 1336, 224, 248),
    };

    // Pattern (0^8 1^8 2^8 1^8)+
    public static RectArea strangeMap15Sprite(long tick) {
        // numFrames = 4, frameDuration = 8
        int index = (int) ((tick % 32) / 8);
        // (0, 1, 2, 3) -> (0, 1, 2, 1)
        if (index == 3) index = 1;
        return STRANGE_MAP_15_SPRITES[index];
    }

    // Map row counts as they appear in the sprite sheet (row by row)
    static final byte[] MAP_ROW_COUNTS = {
        31, 31, 31, 31, 31, 31, 30, 31,
        31, 37, 31, 31, 31, 37, 31, 25,
        37, 31, 37, 37, 37, 37, 37, 31,
        37, 37, 31, 25, 31, 25, 31, 31, 37,
        25, 25, 25, 25,
    };

    // Maze areas where ghosts are shown in maze images, must be masked at runtime
    static final RectArea GHOST_OUTSIDE_HOUSE_AREA = new RectArea(105, 85, 14, 13);
    static final RectArea GHOSTS_INSIDE_HOUSE_AREA = new RectArea(89, 113, 46, 13);

    private static final Map<MazeSpec, ColoredMaze> MAZE_CACHE = new HashMap<>();

    private final Image sourceImage;

    public SpriteSheet_NonArcadeMaps(Image sourceImage) {
        this.sourceImage = checkNotNull(sourceImage);
    }

    private RectArea mazeSprite(int spriteNumber) {
        int colIndex, y;
        switch (spriteNumber) {
            case 1,2,3,4,5,6,7,8            -> { colIndex = (spriteNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { colIndex = (spriteNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { colIndex = (spriteNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { colIndex = (spriteNumber - 25); y = 840;  }
            case 34,35,36,37                -> { colIndex = (spriteNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + spriteNumber);
        }
        int width = 28 * TS, height = MAP_ROW_COUNTS[spriteNumber - 1] * TS;
        return new RectArea(colIndex * width, y, width, height);
    }

    public ColoredMaze miniMaze(int mapNumber, NES_ColorScheme colorScheme) {
        int spriteNumber = switch (mapNumber) {
            case 1 -> 34;
            case 2 -> 35;
            case 3 -> 36;
            case 4 -> 30;
            case 5 -> 28;
            case 6 -> 37;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };
        NES_ColorScheme availableColorScheme = switch (mapNumber) {
            case 1 -> _36_15_20_PINK_RED_WHITE;
            case 2 -> _21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> _35_28_20_PINK_YELLOW_WHITE;
            case 4 -> _28_16_20_YELLOW_RED_WHITE;
            case 5 -> _00_2A_24_GRAY_GREEN_PINK;
            case 6 -> _23_20_2B_VIOLET_WHITE_GREEN;
            default -> null;
        };
        Logger.debug("Get map #{} with color scheme {}", mapNumber, colorScheme);
        return colorScheme.equals(availableColorScheme)
            ? new ColoredMaze(sourceImage, mazeSprite(spriteNumber), colorScheme)
            : getOrCreateMaze(MapCategory.MINI, spriteNumber, colorScheme, availableColorScheme, 13, 23);
    }

    public ColoredMaze bigMaze(int mapNumber, NES_ColorScheme colorScheme) {
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
        NES_ColorScheme availableColorScheme = switch (mapNumber) {
            case  1 -> _07_20_20_BROWN_WHITE_WHITE;
            case  2 -> _15_25_20_RED_ROSE_WHITE;
            case  3 -> _0F_20_1C_BLACK_WHITE_GREEN;
            case  4 -> _19_20_20_GREEN_WHITE_WHITE;
            case  5 -> _0C_20_14_GREEN_WHITE_VIOLET;
            case  6 -> _25_20_20_ROSE_WHITE_WHITE;
            case  7 -> _0F_01_20_BLACK_BLUE_WHITE;
            case  8 -> _28_20_2A_YELLOW_WHITE_GREEN;
            case  9 -> _03_20_20_BLUE_WHITE_WHITE;
            case 10 -> _10_20_28_GRAY_WHITE_YELLOW;
            case 11 -> _15_25_20_RED_ROSE_WHITE;
            default -> null;
        };
        Logger.debug("Get map #{} with color scheme {}", mapNumber, colorScheme);
        return colorScheme.equals(availableColorScheme)
            ? new ColoredMaze(sourceImage, mazeSprite(spriteNumber), colorScheme)
            : getOrCreateMaze(MapCategory.BIG, spriteNumber, colorScheme, availableColorScheme, 13, 32);
    }

    public ColoredMaze strangeMaze(int spriteNumber, NES_ColorScheme randomColorScheme) {
        NES_ColorScheme availableColorScheme = switch (spriteNumber) {
            case 1  -> _36_15_20_PINK_RED_WHITE;
            case 2  -> _21_20_28_BLUE_WHITE_YELLOW;
            case 3  ->  _16_20_15_ORANGE_WHITE_RED;
            case 4  -> _01_38_20_BLUE_YELLOW_WHITE;
            case 5  -> _35_28_20_PINK_YELLOW_WHITE;
            case 6  -> _36_15_20_PINK_RED_WHITE;
            case 7  -> _17_20_20_BROWN_WHITE_WHITE;
            case 8  -> _13_20_28_VIOLET_WHITE_YELLOW;
            case 9  -> _0F_20_28_BLACK_WHITE_YELLOW;
            case 10 -> _0F_01_20_BLACK_BLUE_WHITE;
            case 11 -> _14_25_20_VIOLET_ROSE_WHITE;
            case 12 -> _15_20_20_RED_WHITE_WHITE;
            case 13 -> _1B_20_20_GREEN_WHITE_WHITE;
            case 14 -> _28_20_2A_YELLOW_WHITE_GREEN;
            case 15 -> _1A_20_28_GREEN_WHITE_YELLOW;
            case 16 -> _18_20_20_KHAKI_WHITE_WHITE;
            case 17 -> _25_20_20_ROSE_WHITE_WHITE;
            case 18 -> _12_20_28_BLUE_WHITE_YELLOW;
            case 19 -> _07_20_20_BROWN_WHITE_WHITE;
            case 20 -> _15_25_20_RED_ROSE_WHITE;
            case 21 -> _0F_20_1C_BLACK_WHITE_GREEN;
            case 22 -> _19_20_20_GREEN_WHITE_WHITE;
            case 23 -> _0C_20_14_GREEN_WHITE_VIOLET;
            case 24 -> _23_20_2B_VIOLET_WHITE_GREEN;
            case 25 -> _10_20_28_GRAY_WHITE_YELLOW;
            case 26 -> _03_20_20_BLUE_WHITE_WHITE;
            case 27 -> _04_20_20_VIOLET_WHITE_WHITE;
            case 28 -> _00_2A_24_GRAY_GREEN_PINK;
            case 29 -> _21_35_20_BLUE_PINK_WHITE;
            case 30 -> _28_16_20_YELLOW_RED_WHITE;
            case 31 -> _12_16_20_BLUE_RED_WHITE;
            case 32 -> _15_25_20_RED_ROSE_WHITE;
            default -> throw new IllegalArgumentException("Illegal level number: " + spriteNumber);
        };
        NES_ColorScheme colorScheme = randomColorScheme != null ? randomColorScheme : availableColorScheme;
        Logger.debug("Get map #{} with color scheme {}", spriteNumber, availableColorScheme);
        return colorScheme.equals(availableColorScheme)
            ? new ColoredMaze(sourceImage, mazeSprite(spriteNumber), availableColorScheme)
            : getOrCreateMaze(MapCategory.STRANGE, spriteNumber, colorScheme, availableColorScheme, 13, 23); //TODO Ms. Pac-Man tile can vary
    }

    private ColoredMaze getOrCreateMaze(
            MapCategory mapCategory,
            int spriteNumber,
            NES_ColorScheme requiredColorScheme,
            NES_ColorScheme availableColorScheme,
            int pacTileX, int pacTileY) {

        var cacheKey = new MazeSpec(mapCategory, spriteNumber, requiredColorScheme);
        if (!MAZE_CACHE.containsKey(cacheKey)) {
            RectArea mazeSprite = mazeSprite(spriteNumber);
            Image availableMazeImage = subImage(sourceImage, mazeSprite);
            availableMazeImage = maskImage(availableMazeImage, (x, y) -> isPixelMasked(x, y, pacTileX, pacTileY), Color.TRANSPARENT);
            Image recoloredMazeImage = exchange_NESColorScheme(availableMazeImage, availableColorScheme, requiredColorScheme);
            var cacheValue = new ColoredMaze(recoloredMazeImage,
                new RectArea(0, 0, mazeSprite.width(), mazeSprite.height()),
                requiredColorScheme);
            MAZE_CACHE.put(cacheKey, cacheValue);
            Logger.info("Maze recolored to {} and put into cache (size: {})", requiredColorScheme, MAZE_CACHE.size());
        }
        return MAZE_CACHE.get(cacheKey);
    }

    private boolean isPixelMasked(int x, int y, int pacTileX, int pacTileY) {
        return GHOST_OUTSIDE_HOUSE_AREA.contains(x, y) || GHOSTS_INSIDE_HOUSE_AREA.contains(x, y)
            || (pacTileX == x && pacTileY == y);
    }
}