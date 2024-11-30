/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.model.ms_pacman_tengen.MapCategory.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;

/**
 * Package-private class encapsulating map management.
 */
class MapManager {

    private static List<WorldMap> createMaps(Class<?> loadingClass, String pattern, int maxNumber) {
        ArrayList<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= maxNumber; ++num) {
            String path = pattern.formatted(num);
            URL url = loadingClass.getResource(path);
            if (url != null) {
                try {
                    WorldMap worldMap = new WorldMap(url);
                    Logger.info("World map #{} read. URL='{}'", num, url);
                    maps.add(worldMap);
                } catch (IOException x) {
                    Logger.error(x);
                    Logger.error("Could not create world map, url={}", url);
                }
            } else {
                Logger.error("World map #{} could not be read. URL='{}'", num, url);
            }
        }
        maps.trimToSize();
        return maps;
    }

    private final Map<MapCategory, List<WorldMap>> mapRepository = new EnumMap<>(MapCategory.class);

    public MapManager(String mapsRoot) {
        mapRepository.put(MapCategory.ARCADE,  createMaps(getClass(), mapsRoot + "arcade%d.world", 4));
        mapRepository.put(MapCategory.MINI,    createMaps(getClass(), mapsRoot + "mini%d.world", 6));
        mapRepository.put(MapCategory.BIG,     createMaps(getClass(), mapsRoot + "big%d.world", 11));
        mapRepository.put(MapCategory.STRANGE, createMaps(getClass(), mapsRoot + "strange%d.world", 15));
    }

    public WorldMap configureWorldMap(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE  -> configureArcadeWorldMap(levelNumber);
            case STRANGE -> configureStrangeWorldMap(levelNumber);
            case MINI    -> configureMiniWorldMap(levelNumber);
            case BIG     -> configureBigWorldMap(levelNumber);
        };
    }

    private WorldMap configureArcadeWorldMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> configureWorldMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> configureWorldMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> configureWorldMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> configureWorldMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> configureWorldMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> configureWorldMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> configureWorldMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> configureWorldMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> configureWorldMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private WorldMap configureMiniWorldMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configureWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configureWorldMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configureWorldMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configureWorldMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configureWorldMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configureWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> configureWorldMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configureWorldMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configureWorldMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configureWorldMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configureWorldMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configureWorldMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configureWorldMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configureWorldMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configureWorldMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configureWorldMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configureWorldMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configureWorldMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configureWorldMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configureWorldMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configureWorldMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configureWorldMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configureWorldMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configureWorldMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configureWorldMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configureWorldMap(MINI, 2, _04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> configureWorldMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> configureWorldMap(MINI, 4, NES_ColorScheme.random(), true);
            case 29 -> configureWorldMap(MINI, 5, NES_ColorScheme.random(), true);
            case 30 -> configureWorldMap(MINI, 2, NES_ColorScheme.random(), true);
            case 31 -> configureWorldMap(MINI, 3, NES_ColorScheme.random(), true);
            case 32 -> configureWorldMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private WorldMap configureBigWorldMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configureWorldMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configureWorldMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configureWorldMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configureWorldMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configureWorldMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configureWorldMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> configureWorldMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configureWorldMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configureWorldMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configureWorldMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configureWorldMap(BIG,  5, _15_25_20_RED_ROSE_WHITE);
            case 12 -> configureWorldMap(BIG,  3, _25_20_20_ROSE_WHITE_WHITE);
            case 13 -> configureWorldMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configureWorldMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configureWorldMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configureWorldMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configureWorldMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configureWorldMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configureWorldMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configureWorldMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configureWorldMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configureWorldMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configureWorldMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configureWorldMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configureWorldMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configureWorldMap(BIG, 10, _04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> configureWorldMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> configureWorldMap(BIG,  5, NES_ColorScheme.random(), true);
            case 29 -> configureWorldMap(BIG,  9, NES_ColorScheme.random(), true);
            case 30 -> configureWorldMap(BIG,  2, NES_ColorScheme.random(), true);
            case 31 -> configureWorldMap(BIG, 10, NES_ColorScheme.random(), true);
            case 32 -> configureWorldMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap configureStrangeWorldMap(int levelNumber) {
        WorldMap worldMap = switch (levelNumber) {
            case  1 -> configureWorldMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> configureWorldMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> configureWorldMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> configureWorldMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> configureWorldMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> configureWorldMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> configureWorldMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> configureWorldMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> configureWorldMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configureWorldMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configureWorldMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configureWorldMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configureWorldMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configureWorldMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configureWorldMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configureWorldMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configureWorldMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configureWorldMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configureWorldMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configureWorldMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configureWorldMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configureWorldMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configureWorldMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configureWorldMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configureWorldMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configureWorldMap(BIG,      9, _03_20_20_VIOLET_WHITE_WHITE);
            case 27 -> configureWorldMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> configureWorldMap(MINI,     5, NES_ColorScheme.random(), true);
            case 29 -> configureWorldMap(STRANGE,  8, NES_ColorScheme.random(), true);
            case 30 -> configureWorldMap(MINI,     4, NES_ColorScheme.random(), true);
            case 31 -> configureWorldMap(STRANGE, 12, NES_ColorScheme.random(), true);
            case 32 -> configureWorldMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map such that the renderer can easily determine the map sprite
        worldMap.setConfigValue("levelNumber", levelNumber);
        return worldMap;
    }

    private WorldMap configureWorldMap(MapCategory category, int number, NES_ColorScheme colorScheme, boolean randomColorScheme) {
        WorldMap worldMap = new WorldMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("randomColorScheme", randomColorScheme);
        return worldMap;
    }

    private WorldMap configureWorldMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        return configureWorldMap(category, number, colorScheme, false);
    }
}