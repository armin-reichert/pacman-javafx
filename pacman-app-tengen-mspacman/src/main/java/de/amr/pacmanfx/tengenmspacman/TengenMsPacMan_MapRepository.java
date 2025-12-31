/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.*;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import org.tinylog.Logger;

import java.util.List;

public class TengenMsPacMan_MapRepository implements Disposable {

    public static final TengenMsPacMan_MapRepository INSTANCE = new TengenMsPacMan_MapRepository();

    private final MapColoringService coloringService = new MapColoringService();

    private static NES_ColorScheme colorSchemeOfNonArcadeMap(NonArcadeMapsSpriteSheet.MazeID mazeID){
        return switch (mazeID) {
            case MAZE1           -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE2           -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case MAZE3           -> NES_ColorScheme._16_20_15_ORANGE_WHITE_RED;
            case MAZE4           -> NES_ColorScheme._01_38_20_BLUE_YELLOW_WHITE;
            case MAZE5           -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case MAZE6           -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE7           -> NES_ColorScheme._17_20_20_BROWN_WHITE_WHITE;
            case MAZE8           -> NES_ColorScheme._13_20_28_VIOLET_WHITE_YELLOW;
            case MAZE9           -> NES_ColorScheme._0F_20_28_BLACK_WHITE_YELLOW;
            case MAZE10_BIG      -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case MAZE11          -> NES_ColorScheme._14_25_20_VIOLET_ROSE_WHITE;
            case MAZE12          -> NES_ColorScheme._15_20_20_RED_WHITE_WHITE;
            case MAZE13          -> NES_ColorScheme._1B_20_20_GREEN_WHITE_WHITE;
            case MAZE14_BIG      -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case MAZE15          -> NES_ColorScheme._1A_20_28_GREEN_WHITE_YELLOW;
            case MAZE16_MINI     -> NES_ColorScheme._18_20_20_KHAKI_WHITE_WHITE;
            case MAZE17_BIG      -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case MAZE18          -> NES_ColorScheme._12_20_28_BLUE_WHITE_YELLOW;
            case MAZE19_BIG      -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case MAZE20_BIG      -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case MAZE21_BIG      -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case MAZE22_BIG      -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case MAZE23_BIG      -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case MAZE24          -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            case MAZE25_BIG      -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case MAZE26_BIG      -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case MAZE27          -> NES_ColorScheme._04_20_20_VIOLET_WHITE_WHITE;
            case MAZE28_MINI     -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case MAZE29          -> NES_ColorScheme._21_35_20_BLUE_PINK_WHITE;
            case MAZE30_MINI     -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case MAZE31          -> NES_ColorScheme._12_16_20_BLUE_RED_WHITE;
            case MAZE32_ANIMATED -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;

            default -> throw new IllegalArgumentException("Illegal non-Arcade maze ID: " + mazeID);
        };
    }

    private TengenMsPacMan_MapRepository() {}

    @Override
    public void dispose() {
        Logger.info("Dispose Tengen map repository {}:", getClass().getSimpleName());
        coloringService.dispose();
    }

    /*
     * API to access the maze images stored in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
     * These files contain the images for all mazes used in the different map categories, but only in the colors
     * used by the STRANGE maps through levels 1-32 (for levels 28-31, random color schemes are used.)
     * <p>The MINI and BIG maps use different color schemes.
     * <p>Because the map images do not cover all required map/color-scheme combinations, an image cache is provided where
     * the recolored maze images are stored.
     */
    public MazeSpriteSet createMazeSpriteSet(WorldMap worldMap, int flashCount) {
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
                final NonArcadeMapsSpriteSheet.MazeID mazeID = worldMap.getConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MAZE_ID); // set by map selector!
                yield strangeMazeSpriteSet(
                        mazeID,
                        randomFlashColors ? requestedColorScheme : null,
                        flashCount,
                        randomFlashColors);
            }
        };
    }

    private MazeSpriteSet arcadeMazeSpriteSet(int mapNumber, NES_ColorScheme colorScheme, int flashCount) {
        // All requested maze color schemes exist in the sprite sheet, we only have to select the right sprite for the
        // requested (map number, color scheme) combination:

        final ArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case 1 -> ArcadeMapsSpriteSheet.MazeID.MAZE1;
            case 2 -> ArcadeMapsSpriteSheet.MazeID.MAZE2;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> ArcadeMapsSpriteSheet.MazeID.MAZE3;
                case _35_28_20_PINK_YELLOW_WHITE  -> ArcadeMapsSpriteSheet.MazeID.MAZE5;
                case _17_20_20_BROWN_WHITE_WHITE  -> ArcadeMapsSpriteSheet.MazeID.MAZE7;
                case _0F_20_28_BLACK_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MazeID.MAZE9;
                default -> throw new IllegalArgumentException("No maze image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> ArcadeMapsSpriteSheet.MazeID.MAZE4;
                case _36_15_20_PINK_RED_WHITE      -> ArcadeMapsSpriteSheet.MazeID.MAZE6;
                case _13_20_28_VIOLET_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MazeID.MAZE8;
                default -> throw new IllegalArgumentException("No maze image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };

        final RectShort mazeSprite = ArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID);
        final var coloredMaze = new ColoredSpriteImage(ArcadeMapsSpriteSheet.INSTANCE.sourceImage(), mazeSprite, colorScheme);

        //TODO: Handle case when color scheme is already black & white
        final List<ColoredSpriteImage> flashingMazes = coloringService.createFlashingMazeList(
                MapCategory.ARCADE, mazeID,
                ArcadeMapsSpriteSheet.INSTANCE, mazeSprite,
                colorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK,
                false, flashCount);

        return new MazeSpriteSet(coloredMaze, flashingMazes);
    }

    private MazeSpriteSet miniMazeSpriteSet(
            int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE37_MINI;
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

        return coloringService.createMazeSet(
                MapCategory.MINI, mazeID,
                NonArcadeMapsSpriteSheet.INSTANCE, NonArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID),
                originalColorScheme, requestedColorScheme,
                randomFlashColors, flashCount
        );
    }

    private MazeSpriteSet bigMazeSpriteSet(
            int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case  1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE19_BIG;
            case  2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE20_BIG;
            case  3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE21_BIG;
            case  4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE22_BIG;
            case  5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE23_BIG;
            case  6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE17_BIG;
            case  7 -> NonArcadeMapsSpriteSheet.MazeID.MAZE10_BIG;
            case  8 -> NonArcadeMapsSpriteSheet.MazeID.MAZE14_BIG;
            case  9 -> NonArcadeMapsSpriteSheet.MazeID.MAZE26_BIG;
            case 10 -> NonArcadeMapsSpriteSheet.MazeID.MAZE25_BIG;
            case 11 -> NonArcadeMapsSpriteSheet.MazeID.MAZE33_BIG;
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

        return coloringService.createMazeSet(
                MapCategory.BIG, mazeID,
                NonArcadeMapsSpriteSheet.INSTANCE, NonArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID),
                originalColorScheme, requestedColorScheme,
                randomFlashColors, flashCount
        );
    }

    private MazeSpriteSet strangeMazeSpriteSet(
            NonArcadeMapsSpriteSheet.MazeID mazeID,
            NES_ColorScheme optionalRandomColorScheme,
            int flashCount,
            boolean randomFlashColors) {

        final RectShort mazeSprite = mazeID == NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED
                ? NonArcadeMapsSpriteSheet.INSTANCE.spriteSequence(mazeID)[0]
                : NonArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID);

        final NES_ColorScheme original = colorSchemeOfNonArcadeMap(mazeID);
        final NES_ColorScheme requested = optionalRandomColorScheme == null ? original : optionalRandomColorScheme;

        return coloringService.createMazeSet(
                MapCategory.STRANGE, mazeID,
                NonArcadeMapsSpriteSheet.INSTANCE, mazeSprite,
                original, requested,
                randomFlashColors, flashCount
        );
    }
}
