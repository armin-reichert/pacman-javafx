/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.*;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.util.List;

/**
 * Provides correctly colored and flashing maze sprite sets for the Tengen NES Ms. Pac-Man port.
 * Handles the four map categories (ARCADE, MINI, BIG, STRANGE) and their unique color schemes,
 * including random recoloring in levels 28–31 and the animated "psychedelic" maze #32.
 */
public class TengenMsPacMan_MapRepository {

    public static final TengenMsPacMan_MapRepository INSTANCE = new TengenMsPacMan_MapRepository();

    private TengenMsPacMan_MapRepository() {}

    /*
     * API to access the maze images stored in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
     * These files contain the images for all mazes used in the different map categories, but only in the colors
     * used by the STRANGE maps through levels 1-32 (for levels 28-31, random color schemes are used.)
     * <p>The MINI and BIG maps use different color schemes.
     * <p>Because the map images do not cover all required map/color-scheme combinations, an image cache is provided where
     * the recolored maze images are stored.
     */
    public MapImageSet createMazeSpriteSet(WorldMap worldMap, int flashCount) {
        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MAP_CATEGORY);
        final int mapNumber = worldMap.getConfigValue(GameUI_Config.ConfigKey.MAP_NUMBER);
        final NES_ColorScheme requestedColorScheme = worldMap.getConfigValue(TengenMsPacMan_UIConfig.ConfigKey.NES_COLOR_SCHEME);
        // for randomly colored maps (levels 28-31, non-ARCADE maps), multiple random flash colors appear
        final boolean randomFlashColors = worldMap.getConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MULTIPLE_FLASH_COLORS);

        return switch (mapCategory) {
            case ARCADE  -> arcadeMazeSpriteSet(mapNumber, requestedColorScheme, flashCount);
            case MINI    -> miniMazeSpriteSet(mapNumber, requestedColorScheme, flashCount, randomFlashColors);
            case BIG     -> bigMazeSpriteSet(mapNumber, requestedColorScheme, flashCount, randomFlashColors);
            case STRANGE -> {
                final NonArcadeMapsSpriteSheet.MapID mapID = worldMap.getConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MAP_ID); // set by map selector!
                yield strangeMazeSpriteSet(
                        mapID,
                        randomFlashColors ? requestedColorScheme : null,
                        flashCount,
                        randomFlashColors);
            }
        };
    }

    private MapImageSet arcadeMazeSpriteSet(int mapNumber, NES_ColorScheme colorScheme, int flashCount) {
        // All requested maze color schemes exist in the sprite sheet, we only have to select the right sprite for the
        // requested (map number, color scheme) combination:

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

        final RectShort mazeSprite = ArcadeMapsSpriteSheet.INSTANCE.sprite(mapID);
        final var coloredMaze = new ColorSchemedImage(ArcadeMapsSpriteSheet.INSTANCE.sourceImage(), mazeSprite, colorScheme);

        //TODO: Handle case when color scheme is already black & white
        final List<ColorSchemedImage> flashingMazes = MapColoringService.INSTANCE.createFlashingMapImages(
                MapCategory.ARCADE, mapID,
                ArcadeMapsSpriteSheet.INSTANCE, mazeSprite,
                colorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK,
                false, flashCount);

        return new MapImageSet(coloredMaze, flashingMazes);
    }

    private MapImageSet miniMazeSpriteSet(
            int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MapID mapID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MapID.MAP34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MapID.MAP35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MapID.MAP36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MapID.MAP30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MapID.MAP28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MapID.MAP37_MINI;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        final NES_ColorScheme originalColorScheme = switch (mapNumber) {
            case 1 -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 2 -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case 4 -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case 5 -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case 6 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        return MapColoringService.INSTANCE.createMazeSet(
                MapCategory.MINI, mapID,
                NonArcadeMapsSpriteSheet.INSTANCE, NonArcadeMapsSpriteSheet.INSTANCE.sprite(mapID),
                originalColorScheme, requestedColorScheme,
                randomFlashColors, flashCount
        );
    }

    private MapImageSet bigMazeSpriteSet(
            int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

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

        final NES_ColorScheme originalColorScheme = switch (mapNumber) {
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

        return MapColoringService.INSTANCE.createMazeSet(
                MapCategory.BIG, mapID,
                NonArcadeMapsSpriteSheet.INSTANCE, NonArcadeMapsSpriteSheet.INSTANCE.sprite(mapID),
                originalColorScheme, requestedColorScheme,
                randomFlashColors, flashCount
        );
    }

    private MapImageSet strangeMazeSpriteSet(
            NonArcadeMapsSpriteSheet.MapID mapID,
            NES_ColorScheme optionalRandomColorScheme,
            int flashCount,
            boolean randomFlashColors) {

        final RectShort mazeSprite = mapID == NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED
                ? NonArcadeMapsSpriteSheet.INSTANCE.sprites(mapID)[0]
                : NonArcadeMapsSpriteSheet.INSTANCE.sprite(mapID);

        final NES_ColorScheme original = colorSchemeOfNonArcadeMap(mapID);
        final NES_ColorScheme requested = optionalRandomColorScheme == null ? original : optionalRandomColorScheme;

        return MapColoringService.INSTANCE.createMazeSet(
                MapCategory.STRANGE, mapID,
                NonArcadeMapsSpriteSheet.INSTANCE, mazeSprite,
                original, requested,
                randomFlashColors, flashCount
        );
    }

    private NES_ColorScheme colorSchemeOfNonArcadeMap(NonArcadeMapsSpriteSheet.MapID mapID){
        return switch (mapID) {
            case MAP1           -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAP2           -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case MAP3           -> NES_ColorScheme._16_20_15_ORANGE_WHITE_RED;
            case MAP4           -> NES_ColorScheme._01_38_20_BLUE_YELLOW_WHITE;
            case MAP5           -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case MAP6           -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAP7           -> NES_ColorScheme._17_20_20_BROWN_WHITE_WHITE;
            case MAP8           -> NES_ColorScheme._13_20_28_VIOLET_WHITE_YELLOW;
            case MAP9           -> NES_ColorScheme._0F_20_28_BLACK_WHITE_YELLOW;
            case MAP10_BIG      -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case MAP11          -> NES_ColorScheme._14_25_20_VIOLET_ROSE_WHITE;
            case MAP12          -> NES_ColorScheme._15_20_20_RED_WHITE_WHITE;
            case MAP13          -> NES_ColorScheme._1B_20_20_GREEN_WHITE_WHITE;
            case MAP14_BIG      -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case MAP15          -> NES_ColorScheme._1A_20_28_GREEN_WHITE_YELLOW;
            case MAP16_MINI     -> NES_ColorScheme._18_20_20_KHAKI_WHITE_WHITE;
            case MAP17_BIG      -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case MAP18          -> NES_ColorScheme._12_20_28_BLUE_WHITE_YELLOW;
            case MAP19_BIG      -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case MAP20_BIG      -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case MAP21_BIG      -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case MAP22_BIG      -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case MAP23_BIG      -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case MAP24          -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            case MAP25_BIG      -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case MAP26_BIG      -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case MAP27          -> NES_ColorScheme._04_20_20_VIOLET_WHITE_WHITE;
            case MAP28_MINI     -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case MAP29          -> NES_ColorScheme._21_35_20_BLUE_PINK_WHITE;
            case MAP30_MINI     -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case MAP31          -> NES_ColorScheme._12_16_20_BLUE_RED_WHITE;
            case MAP32_ANIMATED -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;

            default -> throw new IllegalArgumentException("Illegal non-Arcade maze ID: " + mapID);
        };
    }
}
