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

    private static List<WorldMap> createMaps(Class<?> loadingClass, String pattern, int count) {
        ArrayList<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= count; ++num) {
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
                Logger.error("World map #{} could not be read. path='{}'", num, path);
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

    public WorldMap coloredWorldMap(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE  -> arcadeWorldMap(levelNumber);
            case STRANGE -> strangeWorldMap(levelNumber);
            case MINI    -> miniWorldMap(levelNumber);
            case BIG     -> bigWorldMap(levelNumber);
        };
    }

    private WorldMap arcadeWorldMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> coloredWorldMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> coloredWorldMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> coloredWorldMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> coloredWorldMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> coloredWorldMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> coloredWorldMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> coloredWorldMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> coloredWorldMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> coloredWorldMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private WorldMap miniWorldMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> coloredWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> coloredWorldMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> coloredWorldMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> coloredWorldMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> coloredWorldMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> coloredWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> coloredWorldMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> coloredWorldMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> coloredWorldMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> coloredWorldMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> coloredWorldMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> coloredWorldMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> coloredWorldMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> coloredWorldMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> coloredWorldMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> coloredWorldMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> coloredWorldMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> coloredWorldMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> coloredWorldMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> coloredWorldMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> coloredWorldMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> coloredWorldMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> coloredWorldMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> coloredWorldMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> coloredWorldMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> coloredWorldMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> coloredWorldMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> coloredWorldMapWithRandomColorScheme(MINI, 4);
            case 29 -> coloredWorldMapWithRandomColorScheme(MINI, 5);
            case 30 -> coloredWorldMapWithRandomColorScheme(MINI, 2);
            case 31 -> coloredWorldMapWithRandomColorScheme(MINI, 3);
            case 32 -> coloredWorldMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private WorldMap bigWorldMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> coloredWorldMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> coloredWorldMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> coloredWorldMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> coloredWorldMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> coloredWorldMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> coloredWorldMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> coloredWorldMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> coloredWorldMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> coloredWorldMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> coloredWorldMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> coloredWorldMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> coloredWorldMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> coloredWorldMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> coloredWorldMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> coloredWorldMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> coloredWorldMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> coloredWorldMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> coloredWorldMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> coloredWorldMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> coloredWorldMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> coloredWorldMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> coloredWorldMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> coloredWorldMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> coloredWorldMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> coloredWorldMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> coloredWorldMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> coloredWorldMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> coloredWorldMapWithRandomColorScheme(BIG,  5);
            case 29 -> coloredWorldMapWithRandomColorScheme(BIG,  9);
            case 30 -> coloredWorldMapWithRandomColorScheme(BIG,  2);
            case 31 -> coloredWorldMapWithRandomColorScheme(BIG, 10);
            case 32 -> coloredWorldMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap strangeWorldMap(int levelNumber) {
        WorldMap worldMap = switch (levelNumber) {
            case  1 -> coloredWorldMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> coloredWorldMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> coloredWorldMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> coloredWorldMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> coloredWorldMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> coloredWorldMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> coloredWorldMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> coloredWorldMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> coloredWorldMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> coloredWorldMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> coloredWorldMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> coloredWorldMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> coloredWorldMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> coloredWorldMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> coloredWorldMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> coloredWorldMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> coloredWorldMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> coloredWorldMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> coloredWorldMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> coloredWorldMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> coloredWorldMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> coloredWorldMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> coloredWorldMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> coloredWorldMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> coloredWorldMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> coloredWorldMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> coloredWorldMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> coloredWorldMapWithRandomColorScheme(MINI,     5);
            case 29 -> coloredWorldMapWithRandomColorScheme(STRANGE,  8);
            case 30 -> coloredWorldMapWithRandomColorScheme(MINI,     4);
            case 31 -> coloredWorldMapWithRandomColorScheme(STRANGE, 11);
            case 32 -> coloredWorldMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map such that the renderer can easily determine the map sprite
        worldMap.setConfigValue("levelNumber", levelNumber);
        return worldMap;
    }

    private WorldMap coloredWorldMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = new WorldMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("randomColorScheme", false);
        return worldMap;
    }

    private WorldMap coloredWorldMapWithRandomColorScheme(MapCategory category, int number) {
        WorldMap worldMap = coloredWorldMap(category, number, NES_ColorScheme.random());
        worldMap.setConfigValue("randomColorScheme", true);
        return worldMap;
    }
}