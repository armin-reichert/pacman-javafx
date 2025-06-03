/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.MapSelector;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.nes.NES_ColorScheme.*;
import static de.amr.pacmanfx.tengen.ms_pacman.MapCategory.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_MapSelector implements MapSelector {

    private static final String MAP_ROOT_PATH = "/de/amr/pacmanfx/tengen/ms_pacman/maps/";

    private final Map<MapCategory, List<WorldMap>> mapRepository = new EnumMap<>(MapCategory.class);

    @Override
    public List<WorldMap> builtinMaps() {
        List<WorldMap> maps = new ArrayList<>();
        for (MapCategory category : MapCategory.values()) {
            maps.addAll(mapRepository.get(category));
        }
        return maps;
    }

    @Override
    public List<WorldMap> customMaps() {
        return List.of();
    }

    @Override
    public void loadAllMaps() {
        if (mapRepository.isEmpty()) {
            mapRepository.put(MapCategory.ARCADE,  loadMaps(MAP_ROOT_PATH + "arcade%d.world", 4));
            mapRepository.put(MapCategory.MINI,    loadMaps(MAP_ROOT_PATH + "mini%d.world", 6));
            mapRepository.put(MapCategory.BIG,     loadMaps(MAP_ROOT_PATH + "big%02d.world", 11));
            mapRepository.put(MapCategory.STRANGE, loadMaps(MAP_ROOT_PATH + "strange%02d.world", 15));
        }
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public WorldMap findWorldMap(int levelNumber) {
        throw new UnsupportedOperationException(); //TODO ugly! Reconsider API
    }

    public WorldMap createWorldMapForLevel(MapCategory mapCategory, int levelNumber) {
        requireNonNull(mapCategory);
        requireValidLevelNumber(levelNumber);
        return switch (mapCategory) {
            case ARCADE  -> createArcadeMap(levelNumber);
            case STRANGE -> createStrangeMap(levelNumber);
            case MINI    -> createMiniMap(levelNumber);
            case BIG     -> createBigMap(levelNumber);
        };
    }

    private List<WorldMap> loadMaps(String pattern, int count) {
        ArrayList<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= count; ++num) {
            String path = pattern.formatted(num);
            URL url = getClass().getResource(path);
            if (url != null) {
                try {
                    maps.add(WorldMap.fromURL(url));
                    Logger.info("Map #{} loaded, URL={}", num, url);
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

    private WorldMap createArcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> createColoredMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> createColoredMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> createColoredMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> createColoredMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> createColoredMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> createColoredMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> createColoredMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> createColoredMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> createColoredMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createColoredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> createColoredMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createColoredMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> createColoredMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createColoredMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createColoredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> createColoredMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createColoredMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createColoredMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createColoredMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createColoredMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createColoredMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createColoredMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createColoredMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createColoredMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createColoredMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createColoredMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createColoredMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createColoredMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createColoredMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createColoredMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createColoredMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createColoredMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createColoredMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createColoredMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createColoredMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createColoredMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredMap(MINI, 4);
            case 29 -> createRandomlyColoredMap(MINI, 5);
            case 30 -> createRandomlyColoredMap(MINI, 2);
            case 31 -> createRandomlyColoredMap(MINI, 3);
            case 32 -> createColoredMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createColoredMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> createColoredMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createColoredMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> createColoredMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createColoredMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createColoredMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> createColoredMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createColoredMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createColoredMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createColoredMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createColoredMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createColoredMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createColoredMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createColoredMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createColoredMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createColoredMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createColoredMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createColoredMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createColoredMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createColoredMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createColoredMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createColoredMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createColoredMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createColoredMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createColoredMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createColoredMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createColoredMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredMap(BIG,  5);
            case 29 -> createRandomlyColoredMap(BIG,  9);
            case 30 -> createRandomlyColoredMap(BIG,  2);
            case 31 -> createRandomlyColoredMap(BIG, 10);
            case 32 -> createColoredMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createStrangeMap(int levelNumber) {
        WorldMap worldMap = switch (levelNumber) {
            case  1 -> createColoredMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> createColoredMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> createColoredMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> createColoredMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> createColoredMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> createColoredMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> createColoredMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> createColoredMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> createColoredMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createColoredMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createColoredMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createColoredMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createColoredMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createColoredMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createColoredMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createColoredMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createColoredMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createColoredMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createColoredMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createColoredMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createColoredMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createColoredMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createColoredMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createColoredMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createColoredMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createColoredMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createColoredMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredMap(MINI,     5);
            case 29 -> createRandomlyColoredMap(STRANGE,  8);
            case 30 -> createRandomlyColoredMap(MINI,     4);
            case 31 -> createRandomlyColoredMap(STRANGE, 11);
            case 32 -> createColoredMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // Hack: Store level number in map too such that the renderer can easily determine the corresponding map sprite
        worldMap.setConfigValue("levelNumber", levelNumber);
        return worldMap;
    }

    private WorldMap createColoredMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = WorldMap.copyMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("multipleFlashColors", false);
        return worldMap;
    }

    private WorldMap createRandomlyColoredMap(MapCategory category, int number) {
        WorldMap worldMap = createColoredMap(category, number, NES_ColorScheme.random());
        worldMap.setConfigValue("multipleFlashColors", true);
        return worldMap;
    }
}