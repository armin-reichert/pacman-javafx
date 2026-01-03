/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.rendering.NonArcadeMapsSpriteSheet;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.io.IOException;
import java.util.List;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.nes.NES_ColorScheme.*;
import static de.amr.pacmanfx.model.world.WorldMapSelector.loadMaps;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.MAPS_PATH;
import static de.amr.pacmanfx.tengenmspacman.model.MapCategory.*;

/**
 * Selects and configures the correct maze for each level in the Tengen NES Ms. Pac-Man port.
 * Handles the complex progression across 32 levels using four map categories (ARCADE, MINI, BIG, STRANGE)
 * with specific color schemes and random recoloring in levels 28–31.
 */
public class TengenMsPacMan_MapSelector implements WorldMapSelector {

    private List<WorldMap> arcadeMapPrototypes;
    private List<WorldMap> miniMapPrototypes;
    private List<WorldMap> bigMapPrototypes;
    private List<WorldMap> strangeMapPrototypes;

    private void ensureArcadeMapPrototypesLoaded() throws IOException {
        if (arcadeMapPrototypes == null) {
            arcadeMapPrototypes = loadMaps(getClass(), MAPS_PATH + "arcade%d.world", 4);
        }
    }

    private void ensureMiniMapPrototypesLoaded() throws IOException {
        if (miniMapPrototypes == null) {
            miniMapPrototypes = loadMaps(getClass(), MAPS_PATH + "mini%d.world", 6);
        }
    }

    private void ensureBigMapPrototypesLoaded() throws IOException {
        if (bigMapPrototypes == null) {
            bigMapPrototypes = loadMaps(getClass(), MAPS_PATH + "big%02d.world", 11);
        }
    }

    private void ensureStrangeMapPrototypesLoaded() throws IOException {
        if (strangeMapPrototypes == null) {
            strangeMapPrototypes = loadMaps(getClass(), MAPS_PATH + "strange%02d.world", 15);
        }
    }

    @Override
    public void loadMapPrototypes() throws IOException {
        ensureArcadeMapPrototypesLoaded();
        ensureMiniMapPrototypesLoaded();
        ensureBigMapPrototypesLoaded();
        ensureStrangeMapPrototypesLoaded();
    }

    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) throws IOException {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Insufficient information for computing map");
        }
        if (args[0] instanceof MapCategory mapCategory) {
            requireValidLevelNumber(levelNumber);
            return switch (mapCategory) {
                case ARCADE -> {
                    ensureArcadeMapPrototypesLoaded();
                    yield configuredArcadeMap(levelNumber);
                }
                case MINI -> {
                    ensureMiniMapPrototypesLoaded();
                    yield configuredMiniMap(levelNumber);
                }
                case BIG -> {
                    ensureBigMapPrototypesLoaded();
                    yield configuredBigMap(levelNumber);
                }
                case STRANGE -> {
                    ensureStrangeMapPrototypesLoaded();
                    final WorldMap strangeMap = configuredStrangeMap(levelNumber);
                    // Hack: Store mazeID in map properties to make renderer happy
                    final var mazeID = NonArcadeMapsSpriteSheet.MazeID.values()[levelNumber - 1];
                    strangeMap.setConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MAZE_ID, mazeID);
                    yield strangeMap;
                }
            };
        } else {
            throw new IllegalArgumentException("Supplied argument '%s' is not a map category".formatted(args[0]));
        }
    }

    private WorldMap configuredMap(MapCategory category, int number, NES_ColorScheme nesColorScheme) {
        // It is safe to assume prototypes have been loaded
        final List<WorldMap> prototypes = switch (category) {
            case ARCADE -> arcadeMapPrototypes;
            case MINI -> miniMapPrototypes;
            case BIG -> bigMapPrototypes;
            case STRANGE -> strangeMapPrototypes;
        };
        final var worldMap = new WorldMap(prototypes.get(number - 1));
        worldMap.setConfigValue(GameUI_Config.ConfigKey.MAP_NUMBER, number);
        worldMap.setConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MAP_CATEGORY, category);
        worldMap.setConfigValue(TengenMsPacMan_UIConfig.ConfigKey.NES_COLOR_SCHEME, nesColorScheme);
        worldMap.setConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MULTIPLE_FLASH_COLORS, false);
        return worldMap;
    }

    private WorldMap randomlyConfiguredMap(MapCategory category, int number) {
        final WorldMap worldMap = configuredMap(category, number, NES_ColorScheme.randomScheme());
        worldMap.setConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MULTIPLE_FLASH_COLORS, true);
        return worldMap;
    }

    private WorldMap configuredArcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> configuredMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> configuredMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> configuredMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> configuredMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> configuredMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> configuredMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> configuredMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> configuredMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> configuredMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap configuredMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configuredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configuredMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configuredMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configuredMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configuredMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configuredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> configuredMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configuredMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configuredMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuredMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuredMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuredMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuredMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuredMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuredMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuredMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuredMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuredMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuredMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuredMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuredMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuredMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuredMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuredMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuredMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuredMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuredMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyConfiguredMap(MINI, 4);
            case 29 -> randomlyConfiguredMap(MINI, 5);
            case 30 -> randomlyConfiguredMap(MINI, 2);
            case 31 -> randomlyConfiguredMap(MINI, 3);
            case 32 -> configuredMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap configuredBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configuredMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configuredMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configuredMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configuredMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configuredMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configuredMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> configuredMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configuredMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configuredMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuredMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuredMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuredMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuredMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuredMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuredMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuredMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuredMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuredMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuredMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuredMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuredMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuredMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuredMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuredMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuredMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuredMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuredMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyConfiguredMap(BIG,  5);
            case 29 -> randomlyConfiguredMap(BIG,  9);
            case 30 -> randomlyConfiguredMap(BIG,  2);
            case 31 -> randomlyConfiguredMap(BIG, 10);
            case 32 -> configuredMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap configuredStrangeMap(int levelNumber) {
        return switch (levelNumber) {
            case  1 -> configuredMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> configuredMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> configuredMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> configuredMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> configuredMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> configuredMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> configuredMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> configuredMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> configuredMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuredMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuredMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuredMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuredMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuredMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuredMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuredMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuredMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuredMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuredMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuredMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuredMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuredMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuredMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuredMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuredMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuredMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuredMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyConfiguredMap(MINI,     5);
            case 29 -> randomlyConfiguredMap(STRANGE,  8);
            case 30 -> randomlyConfiguredMap(MINI,     4);
            case 31 -> randomlyConfiguredMap(STRANGE, 11);
            case 32 -> configuredMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }
}