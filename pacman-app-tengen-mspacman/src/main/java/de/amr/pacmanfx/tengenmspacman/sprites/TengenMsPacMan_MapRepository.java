/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManGameVariant;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

import java.util.List;

import static de.amr.pacmanfx.tengenmspacman.sprites.NES_MapColorScheme.*;
import static java.util.Objects.requireNonNull;

/**
 * Provides correctly colored and flashing maze sprite sets for the Tengen Ms. Pac-Man port.
 * <br>
 * Handles the four map categories ARCADE, MINI, BIG, STRANGE and their unique color schemes,
 * including random recoloring in levels 28–31 and the animated "psychedelic" maze #32.
 */
public class TengenMsPacMan_MapRepository {

    private static class LazyThreadSafeSingletonHolder {
        static final TengenMsPacMan_MapRepository SINGLETON = new TengenMsPacMan_MapRepository();
    }

    public static TengenMsPacMan_MapRepository instance() { return LazyThreadSafeSingletonHolder.SINGLETON; }

    private TengenMsPacMan_MapRepository() {}

    /*
     * API to access the maze images stored in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
     * These files contain the images for all mazes used in the different map categories, but only in the colors
     * used by the STRANGE maps through levels 1-32 (for levels 28-31, random color schemes are used.)
     * <p>The MINI and BIG maps use different color schemes.
     * <p>Because the map images do not cover all required map/color-scheme combinations, an image cache is provided where
     * the recolored maze images are stored.
     */
    public MapImageSet createMapImageSet(WorldMap worldMap, int flashCount) {
        requireNonNull(worldMap);

        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacManGameVariant.MapConfigKey.MAP_CATEGORY);
        final int mapNumber           = worldMap.getConfigValue(WorldMapConfigKey.MAP_NUMBER);
        final NES_MapColorScheme reqColorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        // for randomly colored maps (levels 28-31, non-ARCADE maps), multiple random flash colors appear
        final boolean randomFlashColors = worldMap.getConfigValue(TengenMsPacManGameVariant.MapConfigKey.MULTIPLE_FLASH_COLORS);

        return switch (mapCategory) {

            case ARCADE  -> arcadeMapImageSet(mapNumber, reqColorScheme, flashCount);

            case MINI    -> miniMapImageSet(mapNumber, reqColorScheme, flashCount, randomFlashColors);

            case BIG     -> bigMapImageSet(mapNumber, reqColorScheme, flashCount, randomFlashColors);

            case STRANGE -> strangeMapImageSet(
                worldMap.getConfigValue(TengenMsPacManGameVariant.MapConfigKey.MAP_ID), // set by map selector!
                randomFlashColors ? reqColorScheme : null,
                flashCount,
                randomFlashColors);
        };
    }

    // All requested maze color schemes exist in the sprite sheet, we only have to select the right sprite for the
    // requested (map number, color scheme) combination:
    private MapImageSet arcadeMapImageSet(int mapNumber, NES_MapColorScheme colorScheme, int flashCount) {

        final ArcadeMapsSpriteSheet.MapID mapID = switch (mapNumber) {
            case 1 -> ArcadeMapsSpriteSheet.MapID.MAP1;
            case 2 -> ArcadeMapsSpriteSheet.MapID.MAP2;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> ArcadeMapsSpriteSheet.MapID.MAP3;
                case _35_28_20_PINK_YELLOW_WHITE  -> ArcadeMapsSpriteSheet.MapID.MAP5;
                case _17_20_20_BROWN_WHITE_WHITE  -> ArcadeMapsSpriteSheet.MapID.MAP7;
                case _0F_20_28_BLACK_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MapID.MAP9;
                default -> throw new IllegalArgumentException("No maze image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> ArcadeMapsSpriteSheet.MapID.MAP4;
                case _36_15_20_PINK_RED_WHITE      -> ArcadeMapsSpriteSheet.MapID.MAP6;
                case _13_20_28_VIOLET_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MapID.MAP8;
                default -> throw new IllegalArgumentException("No maze image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };

        final RectShort sprite = ArcadeMapsSpriteSheet.instance().findSprite(mapID);
        final var coloredMaze = new ColorSchemedMapSprite(ArcadeMapsSpriteSheet.instance().sourceImage(), sprite, colorScheme);

        //TODO: Handle case when color scheme is already black & white
        final List<ColorSchemedMapSprite> flashingMazes = MapColoringService.instance().createFlashingMapImages(
            MapCategory.ARCADE, mapID,
            ArcadeMapsSpriteSheet.instance(), sprite,
            colorScheme, NES_MapColorScheme._0F_20_0F_BLACK_WHITE_BLACK,
            false, flashCount);

        return new MapImageSet(coloredMaze, flashingMazes);
    }

    private MapImageSet miniMapImageSet(
        int mapNumber, NES_MapColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MapID mapID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MapID.MAP34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MapID.MAP35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MapID.MAP36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MapID.MAP30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MapID.MAP28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MapID.MAP37_MINI;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        final NES_MapColorScheme originalColorScheme = switch (mapNumber) {
            case 1 -> _36_15_20_PINK_RED_WHITE;
            case 2 -> _21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> _35_28_20_PINK_YELLOW_WHITE;
            case 4 -> _28_16_20_YELLOW_RED_WHITE;
            case 5 -> _00_2A_24_GRAY_GREEN_PINK;
            case 6 -> _23_20_2B_VIOLET_WHITE_GREEN;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        return MapColoringService.instance().createMapImageSet(
            MapCategory.MINI, mapID,
            NonArcadeMapsSpriteSheet.instance(), NonArcadeMapsSpriteSheet.instance().findSprite(mapID),
            originalColorScheme, requestedColorScheme,
            randomFlashColors, flashCount
        );
    }

    private MapImageSet bigMapImageSet(
        int mapNumber, NES_MapColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MapID mapID = switch (mapNumber) {
            case  1 -> NonArcadeMapsSpriteSheet.MapID.MAP19_BIG;
            case  2 -> NonArcadeMapsSpriteSheet.MapID.MAP20_BIG;
            case  3 -> NonArcadeMapsSpriteSheet.MapID.MAP21_BIG;
            case  4 -> NonArcadeMapsSpriteSheet.MapID.MAP22_BIG;
            case  5 -> NonArcadeMapsSpriteSheet.MapID.MAP23_BIG;
            case  6 -> NonArcadeMapsSpriteSheet.MapID.MAP17_BIG;
            case  7 -> NonArcadeMapsSpriteSheet.MapID.MAP10_BIG;
            case  8 -> NonArcadeMapsSpriteSheet.MapID.MAP14_BIG;
            case  9 -> NonArcadeMapsSpriteSheet.MapID.MAP26_BIG;
            case 10 -> NonArcadeMapsSpriteSheet.MapID.MAP25_BIG;
            case 11 -> NonArcadeMapsSpriteSheet.MapID.MAP33_BIG;
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + mapNumber);
        };

        final NES_MapColorScheme originalColorScheme = switch (mapNumber) {
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

        return MapColoringService.instance().createMapImageSet(
            MapCategory.BIG, mapID,
            NonArcadeMapsSpriteSheet.instance(), NonArcadeMapsSpriteSheet.instance().findSprite(mapID),
            originalColorScheme, requestedColorScheme,
            randomFlashColors, flashCount
        );
    }

    private MapImageSet strangeMapImageSet(
        NonArcadeMapsSpriteSheet.MapID mapID,
        NES_MapColorScheme optionalRandomColorScheme,
        int flashCount,
        boolean randomFlashColors) {

        final RectShort mazeSprite = mapID == NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED
            ? NonArcadeMapsSpriteSheet.instance().findSprites(mapID)[0]
            : NonArcadeMapsSpriteSheet.instance().findSprite(mapID);

        final NES_MapColorScheme original = colorSchemeOfNonArcadeMap(mapID);
        final NES_MapColorScheme requested = optionalRandomColorScheme == null ? original : optionalRandomColorScheme;

        return MapColoringService.instance().createMapImageSet(
            MapCategory.STRANGE, mapID,
            NonArcadeMapsSpriteSheet.instance(), mazeSprite,
            original, requested,
            randomFlashColors, flashCount
        );
    }

    private NES_MapColorScheme colorSchemeOfNonArcadeMap(NonArcadeMapsSpriteSheet.MapID mapID){
        return switch (mapID) {
            case MAP1           -> _36_15_20_PINK_RED_WHITE;
            case MAP2           -> _21_20_28_BLUE_WHITE_YELLOW;
            case MAP3           -> _16_20_15_ORANGE_WHITE_RED;
            case MAP4           -> _01_38_20_BLUE_YELLOW_WHITE;
            case MAP5           -> _35_28_20_PINK_YELLOW_WHITE;
            case MAP6           -> _36_15_20_PINK_RED_WHITE;
            case MAP7           -> _17_20_20_BROWN_WHITE_WHITE;
            case MAP8           -> _13_20_28_VIOLET_WHITE_YELLOW;
            case MAP9           -> _0F_20_28_BLACK_WHITE_YELLOW;
            case MAP10_BIG      -> _0F_01_20_BLACK_BLUE_WHITE;
            case MAP11          -> _14_25_20_VIOLET_ROSE_WHITE;
            case MAP12          -> _15_20_20_RED_WHITE_WHITE;
            case MAP13          -> _1B_20_20_GREEN_WHITE_WHITE;
            case MAP14_BIG      -> _28_20_2A_YELLOW_WHITE_GREEN;
            case MAP15          -> _1A_20_28_GREEN_WHITE_YELLOW;
            case MAP16_MINI     -> _18_20_20_KHAKI_WHITE_WHITE;
            case MAP17_BIG      -> _25_20_20_ROSE_WHITE_WHITE;
            case MAP18          -> _12_20_28_BLUE_WHITE_YELLOW;
            case MAP19_BIG      -> _07_20_20_BROWN_WHITE_WHITE;
            case MAP20_BIG      -> _15_25_20_RED_ROSE_WHITE;
            case MAP21_BIG      -> _0F_20_1C_BLACK_WHITE_GREEN;
            case MAP22_BIG      -> _19_20_20_GREEN_WHITE_WHITE;
            case MAP23_BIG      -> _0C_20_14_GREEN_WHITE_VIOLET;
            case MAP24          -> _23_20_2B_VIOLET_WHITE_GREEN;
            case MAP25_BIG      -> _10_20_28_GRAY_WHITE_YELLOW;
            case MAP26_BIG      -> _03_20_20_BLUE_WHITE_WHITE;
            case MAP27          -> _04_20_20_VIOLET_WHITE_WHITE;
            case MAP28_MINI     -> _00_2A_24_GRAY_GREEN_PINK;
            case MAP29          -> _21_35_20_BLUE_PINK_WHITE;
            case MAP30_MINI     -> _28_16_20_YELLOW_RED_WHITE;
            case MAP31          -> _12_16_20_BLUE_RED_WHITE;
            case MAP32_ANIMATED -> _15_25_20_RED_ROSE_WHITE;

            default -> throw new IllegalArgumentException("Illegal non-Arcade maze ID: " + mapID);
        };
    }
}
