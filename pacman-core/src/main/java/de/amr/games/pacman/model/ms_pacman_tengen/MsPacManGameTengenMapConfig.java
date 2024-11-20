/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.net.URL;
import java.util.*;

import static de.amr.games.pacman.lib.Globals.inRange;
import static de.amr.games.pacman.model.ms_pacman_tengen.MapCategory.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;

public class MsPacManGameTengenMapConfig {

    public static EnumMap<NES_ColorScheme, Map<String, String>> COLOR_MAPS = new EnumMap<>(NES_ColorScheme.class);
    static {
        for (var nesColorScheme : NES_ColorScheme.values()) {
            COLOR_MAPS.put(nesColorScheme, Map.of(
                "fill",   nesColorScheme.fillColor(),
                "stroke", nesColorScheme.strokeColor(),
                "door",   nesColorScheme.strokeColor(),
                "pellet", nesColorScheme.pelletColor()
            ));
        }
    }

    private static List<WorldMap> createMaps(Class<?> loadingClass, String pattern, int maxNumber) {
        ArrayList<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= maxNumber; ++num) {
            String path = pattern.formatted(num);
            URL url = loadingClass.getResource(path);
            if (url != null) {
                maps.add(new WorldMap(url));
                Logger.info("World map #{} read. URL='{}'", num, url);
            } else {
                Logger.error("World map #{} could not be read. URL='{}'", num, url);
            }
        }
        maps.trimToSize();
        return maps;
    }

    private final List<WorldMap> arcadeMaps, miniMaps, bigMaps, strangeMaps;

    public MsPacManGameTengenMapConfig(String mapsRoot) {
        arcadeMaps  = createMaps(getClass(), mapsRoot + "arcade%d.world", 4);
        miniMaps    = createMaps(getClass(), mapsRoot + "mini%d.world", 6);
        bigMaps     = createMaps(getClass(), mapsRoot + "big%d.world", 11);
        strangeMaps = createMaps(getClass(), mapsRoot + "strange%d.world", 15);
    }

    public Map<String, Object> getMapConfig(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE  -> arcadeMapConfig(levelNumber);
            case STRANGE -> strangeMapConfig(levelNumber);
            case MINI    -> miniMapConfig(levelNumber);
            case BIG     -> bigMapConfig(levelNumber);
        };
    }

    public boolean isRandomColorSchemeUsed(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE -> false; // TODO check
            case BIG, MINI -> inRange(levelNumber, 28, 31);
            case STRANGE -> false; // TODO not true
        };
    }

    private Map<String, Object> arcadeMapConfig(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> cfg(ARCADE, arcadeMaps, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> cfg(ARCADE, arcadeMaps, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> cfg(ARCADE, arcadeMaps, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> cfg(ARCADE, arcadeMaps, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> cfg(ARCADE, arcadeMaps, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> cfg(ARCADE, arcadeMaps, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> cfg(ARCADE, arcadeMaps, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> cfg(ARCADE, arcadeMaps, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> cfg(ARCADE, arcadeMaps, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private Map<String, Object> miniMapConfig(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> cfg(MINI, miniMaps, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> cfg(MINI, miniMaps, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> cfg(MINI, miniMaps, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> cfg(MINI, miniMaps, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> cfg(MINI, miniMaps, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> cfg(MINI, miniMaps, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> cfg(MINI, miniMaps, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> cfg(MINI, miniMaps, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> cfg(MINI, miniMaps, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> cfg(MINI, miniMaps, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> cfg(MINI, miniMaps, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> cfg(MINI, miniMaps, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> cfg(MINI, miniMaps, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> cfg(MINI, miniMaps, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> cfg(MINI, miniMaps, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> cfg(MINI, miniMaps, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> cfg(MINI, miniMaps, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> cfg(MINI, miniMaps, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> cfg(MINI, miniMaps, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> cfg(MINI, miniMaps, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> cfg(MINI, miniMaps, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> cfg(MINI, miniMaps, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> cfg(MINI, miniMaps, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> cfg(MINI, miniMaps, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> cfg(MINI, miniMaps, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> cfg(MINI, miniMaps, 2, _04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> cfg(MINI, miniMaps, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> cfg(MINI, miniMaps, 4, NES_ColorScheme.random());
            case 29 -> cfg(MINI, miniMaps, 5, NES_ColorScheme.random());
            case 30 -> cfg(MINI, miniMaps, 2, NES_ColorScheme.random());
            case 31 -> cfg(MINI, miniMaps, 3, NES_ColorScheme.random());
            case 32 -> cfg(MINI, miniMaps, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private Map<String, Object> bigMapConfig(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> cfg(BIG, bigMaps,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> cfg(BIG, bigMaps,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> cfg(BIG, bigMaps,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> cfg(BIG, bigMaps,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> cfg(BIG, bigMaps,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> cfg(BIG, bigMaps,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> cfg(BIG, bigMaps,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> cfg(BIG, bigMaps,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> cfg(BIG, bigMaps,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> cfg(BIG, bigMaps,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> cfg(BIG, bigMaps,  5, _15_25_20_RED_ROSE_WHITE);
            case 12 -> cfg(BIG, bigMaps,  3, _25_20_20_ROSE_WHITE_WHITE);
            case 13 -> cfg(BIG, bigMaps,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> cfg(BIG, bigMaps,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> cfg(BIG, bigMaps,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> cfg(BIG, bigMaps,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> cfg(BIG, bigMaps,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> cfg(BIG, bigMaps,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> cfg(BIG, bigMaps,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> cfg(BIG, bigMaps,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> cfg(BIG, bigMaps,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> cfg(BIG, bigMaps,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> cfg(BIG, bigMaps,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> cfg(BIG, bigMaps,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> cfg(BIG, bigMaps,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> cfg(BIG, bigMaps, 10, _04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> cfg(BIG, bigMaps,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> cfg(BIG, bigMaps,  5, NES_ColorScheme.random());
            case 29 -> cfg(BIG, bigMaps,  9, NES_ColorScheme.random());
            case 30 -> cfg(BIG, bigMaps,  2, NES_ColorScheme.random());
            case 31 -> cfg(BIG, bigMaps, 10, NES_ColorScheme.random());
            case 32 -> cfg(BIG, bigMaps, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private Map<String, Object> strangeMapConfig(int levelNumber) {
        Map<String, Object> mapConfig = switch (levelNumber) {
            case  1 -> cfg(STRANGE, strangeMaps,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> cfg(STRANGE, strangeMaps,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> cfg(STRANGE, strangeMaps,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> cfg(STRANGE, strangeMaps,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> cfg(STRANGE, strangeMaps,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> cfg(STRANGE, strangeMaps,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> cfg(STRANGE, strangeMaps,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> cfg(STRANGE, strangeMaps,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> cfg(STRANGE, strangeMaps,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> cfg(BIG,     bigMaps,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> cfg(STRANGE, strangeMaps, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> cfg(STRANGE, strangeMaps, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> cfg(STRANGE, strangeMaps,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> cfg(BIG,     bigMaps,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> cfg(STRANGE, strangeMaps, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> cfg(MINI,    miniMaps,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> cfg(BIG,     bigMaps,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> cfg(STRANGE, strangeMaps, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> cfg(BIG,     bigMaps,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> cfg(BIG,     bigMaps,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> cfg(BIG,     bigMaps,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> cfg(BIG,     bigMaps,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> cfg(BIG,     bigMaps,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> cfg(STRANGE, strangeMaps,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> cfg(BIG,     bigMaps,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> cfg(BIG,     bigMaps,      9, _03_20_20_VIOLET_WHITE_WHITE);
            case 27 -> cfg(STRANGE, strangeMaps, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> cfg(MINI,    miniMaps,     5, NES_ColorScheme.random());
            case 29 -> cfg(STRANGE, strangeMaps,  8, NES_ColorScheme.random());
            case 30 -> cfg(MINI,    miniMaps,     4, NES_ColorScheme.random());
            case 31 -> cfg(STRANGE, strangeMaps, 12, NES_ColorScheme.random());
            case 32 -> cfg(STRANGE, strangeMaps, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map config such that the renderer can easily determine the map sprite
        Map<String, Object> extConfig = new HashMap<>(mapConfig);
        extConfig.put("levelNumber", levelNumber);
        return extConfig;
    }

    private Map<String, Object> cfg(MapCategory category, List<WorldMap> maps, int number, NES_ColorScheme colorScheme) {
        return Map.of(
            "mapCategory", category,
            "mapNumber", number,
            "worldMap", new WorldMap(maps.get(number - 1)),
            "nesColorScheme", colorScheme
        );
    }
}