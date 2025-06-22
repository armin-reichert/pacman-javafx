/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColorSchemedSprite;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColoredMapConfiguration;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.Sprite.makeSprite;
import static de.amr.pacmanfx.uilib.Ufx.replaceColors;
import static java.util.Objects.requireNonNull;

/**
 * This is the API to access the maze images in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
 * <p>
 * These PNG files contain images for all mazes of the different map categories.
 * The color schemes correspond to the ones used in STRANGE mode running through all levels (1-32).
 * Because levels 28-31 use random color schemes, and the MINI and BIG categories use their
 * maps in different color schemes, the images in these files do not cover all required map/color scheme
 * combinations.<br>
*  For this reason, a cache is provided where maze images are stored after getting recolored into the color scheme
 * required in a specific game level.
 * </p>
 */
public class TengenMsPacMan_MapRepository {

    private record CacheKey(MapCategory mapCategory, int spriteNumber, NES_ColorScheme colorScheme) {}

    private static final Vector2i ARCADE_MAZE_SIZE = Vector2i.of(28 * TS, 31 * TS);

    // Map row counts as they appear in the sprite sheet (row by row)
    private static final byte[] NON_ARCADE_MAP_ROW_COUNTS = {
        31, 31, 31, 31, 31, 31, 30, 31,
        31, 37, 31, 31, 31, 37, 31, 25,
        37, 31, 37, 37, 37, 37, 37, 31,
        37, 37, 31, 25, 31, 25, 31, 31, 37,
        25, 25, 25, 25,
    };

    // Strange map #15 (level 32) has 3 different images to create an animation effect
    private static final Sprite[] STRANGE_MAP_15_SPRITES = {
        makeSprite(1568, 840, 224, 248), makeSprite(1568, 1088, 224, 248), makeSprite(1568, 1336, 224, 248)
    };

    // Frame pattern: (00000000111111112222222211111111)+, numFrames = 4, frameDuration = 8
    public static Sprite strangeMap15Sprite(long tick) {
        long i = tick % 32; // 0..31
        return STRANGE_MAP_15_SPRITES[i < 8 ? 0 : i < 16 ? 1 : i < 24 ? 2 : 1];
    }

    private static Image crop(Image image, Sprite r) {
        WritableImage result = new WritableImage(r.width(), r.height());
        result.getPixelWriter().setPixels(0, 0, r.width(), r.height(), image.getPixelReader(), r.x(), r.y());
        return result;
    }

    private static NES_ColorScheme colorSchemeFromNonArcadeMapsSpriteSheet(int spriteNumber){
        return switch (spriteNumber) {
            case 1  -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 2  -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case 3  -> NES_ColorScheme._16_20_15_ORANGE_WHITE_RED;
            case 4  -> NES_ColorScheme._01_38_20_BLUE_YELLOW_WHITE;
            case 5  -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case 6  -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 7  -> NES_ColorScheme._17_20_20_BROWN_WHITE_WHITE;
            case 8  -> NES_ColorScheme._13_20_28_VIOLET_WHITE_YELLOW;
            case 9  -> NES_ColorScheme._0F_20_28_BLACK_WHITE_YELLOW;
            case 10 -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case 11 -> NES_ColorScheme._14_25_20_VIOLET_ROSE_WHITE;
            case 12 -> NES_ColorScheme._15_20_20_RED_WHITE_WHITE;
            case 13 -> NES_ColorScheme._1B_20_20_GREEN_WHITE_WHITE;
            case 14 -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case 15 -> NES_ColorScheme._1A_20_28_GREEN_WHITE_YELLOW;
            case 16 -> NES_ColorScheme._18_20_20_KHAKI_WHITE_WHITE;
            case 17 -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case 18 -> NES_ColorScheme._12_20_28_BLUE_WHITE_YELLOW;
            case 19 -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case 20 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case 21 -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case 22 -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case 23 -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case 24 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            case 25 -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case 26 -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case 27 -> NES_ColorScheme._04_20_20_VIOLET_WHITE_WHITE;
            case 28 -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case 29 -> NES_ColorScheme._21_35_20_BLUE_PINK_WHITE;
            case 30 -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case 31 -> NES_ColorScheme._12_16_20_BLUE_RED_WHITE;
            case 32 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            default -> throw new IllegalArgumentException("Illegal sprite number: " + spriteNumber);
        };
    }

    private static List<NES_ColorScheme> randomColorSchemes(int count, NES_ColorScheme colorScheme) {
        var randomColorSchemes = new HashSet<NES_ColorScheme>();
        while (randomColorSchemes.size() < count) {
            NES_ColorScheme randomColorScheme = NES_ColorScheme.random();
            if (!randomColorScheme.equals(colorScheme)) {
                randomColorSchemes.add(randomColorScheme);
            }
        }
        return randomColorSchemes.stream().toList();
    }

    private final Map<CacheKey, ColorSchemedSprite> cache = new HashMap<>();
    private final Image arcadeMapImages;
    private final Image nonArcadeMapImages;

    public TengenMsPacMan_MapRepository(Image arcadeMapImages, Image nonArcadeMapImages) {
        this.arcadeMapImages = requireNonNull(arcadeMapImages);
        this.nonArcadeMapImages = requireNonNull(nonArcadeMapImages);
    }

    public ColoredMapConfiguration createMapConfiguration(WorldMap worldMap, int flashCount) {
        MapCategory mapCategory = worldMap.getConfigValue("mapCategory");
        int mapNumber = worldMap.getConfigValue("mapNumber");
        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
        // for randomly colored maps (levels 28-31, non-ARCADE map categories), multiple flash colors appear
        boolean multipleFlashColors = worldMap.getConfigValue("multipleFlashColors");
        return switch (mapCategory) {
            case ARCADE  -> arcadeMapConfiguration(mapNumber, nesColorScheme, flashCount);
            case MINI    -> miniMapConfiguration(mapNumber, nesColorScheme, flashCount, multipleFlashColors);
            case BIG     -> bigMapConfiguration(mapNumber, nesColorScheme, flashCount, multipleFlashColors);
            case STRANGE -> strangeMapConfiguration(
                worldMap.getConfigValue("levelNumber"),
                multipleFlashColors ? worldMap.getConfigValue("nesColorScheme") : null,
                flashCount,
                multipleFlashColors);
        };
    }

    private ColoredMapConfiguration arcadeMapConfiguration(int mapNumber, NES_ColorScheme colorScheme, int flashCount) {
        int spriteIndex = switch (mapNumber) {
            case 1 -> 0;
            case 2 -> 1;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> 2;
                case _35_28_20_PINK_YELLOW_WHITE  -> 4;
                case _17_20_20_BROWN_WHITE_WHITE  -> 6;
                case _0F_20_28_BLACK_WHITE_YELLOW -> 8;
                default -> throw new IllegalArgumentException("No maze image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> 3;
                case _36_15_20_PINK_RED_WHITE      -> 5;
                case _13_20_28_VIOLET_WHITE_YELLOW -> 7;
                default -> throw new IllegalArgumentException("No maze image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };
        int col = spriteIndex % 3, row = spriteIndex / 3;
        var mazeImageArea = new Sprite(col * ARCADE_MAZE_SIZE.x(), row * ARCADE_MAZE_SIZE.y(), ARCADE_MAZE_SIZE.x(), ARCADE_MAZE_SIZE.y());
        var mazeImageRegion = new ColorSchemedSprite(arcadeMapImages, mazeImageArea, colorScheme);
        var flashingMazeImageRegions = new ArrayList<ColorSchemedSprite>();
        //TODO: Handle case when color scheme is already black & white
        ColorSchemedSprite blackWhiteMazeImageRegion = getCachedImageRegion(
            MapCategory.ARCADE, mapNumber, mazeImageArea, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorScheme);
        for (int i = 0; i < flashCount; ++i) {
            flashingMazeImageRegions.add(blackWhiteMazeImageRegion);
        }
        return new ColoredMapConfiguration(mazeImageRegion, flashingMazeImageRegions);
    }

    private ColoredMapConfiguration miniMapConfiguration(int mapNumber, NES_ColorScheme colorScheme, int flashCount, boolean multipleFlashColors) {
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
            case 1 -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 2 -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case 4 -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case 5 -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case 6 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            default -> null;
        };
        Sprite mazeSprite = nonArcadeMapsSprite(spriteNumber);
        ColorSchemedSprite normalMaze = colorScheme.equals(availableColorScheme)
            ? new ColorSchemedSprite(nonArcadeMapImages, nonArcadeMapsSprite(spriteNumber), colorScheme)
            : getCachedImageRegion(MapCategory.MINI, spriteNumber, mazeSprite, colorScheme, availableColorScheme);

        var flashingMazes = new ArrayList<ColorSchemedSprite>();
        if (multipleFlashColors) {
            for (var randomScheme : randomColorSchemes(flashCount, colorScheme)) {
                ColorSchemedSprite randomMaze = getCachedImageRegion(MapCategory.MINI, spriteNumber, mazeSprite,
                    randomScheme, availableColorScheme);
                flashingMazes.add(randomMaze);
            }
        } else {
            ColorSchemedSprite blackWhiteMaze = getCachedImageRegion(MapCategory.MINI, spriteNumber, mazeSprite,
                NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, availableColorScheme);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazes.add(blackWhiteMaze);
            }
        }
        return new ColoredMapConfiguration(normalMaze, flashingMazes);
    }

    private ColoredMapConfiguration bigMapConfiguration(int mapNumber, NES_ColorScheme colorScheme, int flashCount, boolean multipleFlashColors) {
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
        NES_ColorScheme colorSchemeInSpriteSheet = switch (mapNumber) {
            case  1 -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case  2 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case  3 -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case  4 -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case  5 -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case  6 -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case  7 -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case  8 -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case  9 -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case 10 -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case 11 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            default -> null;
        };
        Sprite mazeSprite = nonArcadeMapsSprite(spriteNumber);
        ColorSchemedSprite normalMaze = colorScheme.equals(colorSchemeInSpriteSheet)
            ? new ColorSchemedSprite(nonArcadeMapImages, nonArcadeMapsSprite(spriteNumber), colorScheme)
            : getCachedImageRegion(MapCategory.BIG, spriteNumber, mazeSprite, colorScheme, colorSchemeInSpriteSheet);

        var flashingMazes = new ArrayList<ColorSchemedSprite>();
        if (multipleFlashColors) {
            for (var randomScheme : randomColorSchemes(flashCount, colorScheme)) {
                ColorSchemedSprite randomMaze = getCachedImageRegion(MapCategory.BIG, spriteNumber, mazeSprite,
                    randomScheme, colorSchemeInSpriteSheet);
                flashingMazes.add(randomMaze);
            }
        } else {
            ColorSchemedSprite blackWhiteMaze = getCachedImageRegion(MapCategory.BIG, spriteNumber, mazeSprite,
                NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorSchemeInSpriteSheet);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazes.add(blackWhiteMaze);
            }
        }
        return new ColoredMapConfiguration(normalMaze, flashingMazes);
    }

    private ColoredMapConfiguration strangeMapConfiguration(
        int spriteNumber,
        NES_ColorScheme randomColorScheme,
        int flashCount,
        boolean multipleFlashColors)
    {
        final Sprite mapSprite = nonArcadeMapsSprite(spriteNumber);
        final NES_ColorScheme colorSchemeFromSpriteSheet = colorSchemeFromNonArcadeMapsSpriteSheet(spriteNumber);
        final NES_ColorScheme requestedColorScheme = randomColorScheme != null ? randomColorScheme : colorSchemeFromSpriteSheet;
        final ColorSchemedSprite mazeImageRegion = requestedColorScheme.equals(colorSchemeFromSpriteSheet)
            ? new ColorSchemedSprite(nonArcadeMapImages, mapSprite, colorSchemeFromSpriteSheet)
            : getCachedImageRegion(MapCategory.STRANGE, spriteNumber, mapSprite, requestedColorScheme, colorSchemeFromSpriteSheet);
        final var flashingMazeImageRegions = new ArrayList<ColorSchemedSprite>();

        if (multipleFlashColors) {
            for (NES_ColorScheme nesColorScheme : randomColorSchemes(flashCount, requestedColorScheme)) {
                ColorSchemedSprite randomMazeRegion = getCachedImageRegion(
                    MapCategory.STRANGE, spriteNumber, mapSprite,
                    nesColorScheme, colorSchemeFromSpriteSheet);
                flashingMazeImageRegions.add(randomMazeRegion);
            }
        } else {
            ColorSchemedSprite blackWhiteMaze = getCachedImageRegion(MapCategory.STRANGE, spriteNumber, mapSprite,
                NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorSchemeFromSpriteSheet);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeImageRegions.add(blackWhiteMaze);
            }
        }
        return new ColoredMapConfiguration(mazeImageRegion, flashingMazeImageRegions);
    }

    private Sprite nonArcadeMapsSprite(int spriteNumber) {
        int colIndex, y;
        switch (spriteNumber) {
            case 1,2,3,4,5,6,7,8            -> { colIndex = (spriteNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { colIndex = (spriteNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { colIndex = (spriteNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { colIndex = (spriteNumber - 25); y = 840;  }
            case 34,35,36,37                -> { colIndex = (spriteNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + spriteNumber);
        }
        int width = 28 * TS, height = NON_ARCADE_MAP_ROW_COUNTS[spriteNumber - 1] * TS;
        return new Sprite(colIndex * width, y, width, height);
    }

    private ColorSchemedSprite getCachedImageRegion(
        MapCategory mapCategory,
        int spriteNumber,
        Sprite sourceArea,
        NES_ColorScheme newColorScheme,
        NES_ColorScheme existingColorScheme)
    {
        var key = new CacheKey(mapCategory, spriteNumber, newColorScheme);
        if (!cache.containsKey(key)) {
            Image mapImages = mapCategory == MapCategory.ARCADE ? arcadeMapImages : nonArcadeMapImages;
            Image mapImage = replaceColors(crop(mapImages, sourceArea), existingColorScheme, newColorScheme);
            Sprite targetArea = new Sprite(0, 0, sourceArea.width(), sourceArea.height());
            var recoloredMapImage = new ColorSchemedSprite(mapImage, targetArea, newColorScheme);
            cache.put(key, recoloredMapImage);
            Logger.info("{} map image recolored to {} and stored in cache (size: {})", mapCategory, newColorScheme, cache.size());
        }
        return cache.get(key);
    }
}