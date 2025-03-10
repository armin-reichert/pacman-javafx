/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapSelector;
import de.amr.games.pacman.tengen.ms_pacman.maps.MapCategory;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.nes.NES_ColorScheme.*;
import static de.amr.games.pacman.tengen.ms_pacman.maps.MapCategory.*;

public class TengenMsPacMan_MapSelector extends MapSelector {

    private static final String MAPS_ROOT = "/de/amr/games/pacman/tengen/ms_pacman/maps/";

    private final Map<MapCategory, List<WorldMap>> mapRepository = new EnumMap<>(MapCategory.class);

    public TengenMsPacMan_MapSelector() {}

    @Override
    public List<WorldMap> builtinMaps() {
        List<WorldMap> all = new ArrayList<>();
        for (MapCategory category : MapCategory.values()) {
            all.addAll(mapRepository.get(category));
        }
        return all;
    }

    @Override
    public List<WorldMap> customMaps() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public void loadAllMaps(GameModel game) {
        if (mapRepository.isEmpty()) {
            mapRepository.put(MapCategory.ARCADE, createMaps(MAPS_ROOT + "arcade%d.world", 4));
            mapRepository.put(MapCategory.MINI, createMaps(MAPS_ROOT + "mini%d.world", 6));
            mapRepository.put(MapCategory.BIG, createMaps(MAPS_ROOT + "big%02d.world", 11));
            mapRepository.put(MapCategory.STRANGE, createMaps(MAPS_ROOT + "strange%02d.world", 15));
        }
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber) {
        throw new UnsupportedOperationException(); //TODO ugly
    }

    public WorldMap selectWorldMap(MapCategory mapCategory, int levelNumber) {
        return coloredWorldMap(mapCategory, levelNumber);
    }

    public WorldMap coloredWorldMap(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE  -> coloredArcadeMap(levelNumber);
            case STRANGE -> coloredStrangeMap(levelNumber);
            case MINI    -> coloredMiniMap(levelNumber);
            case BIG     -> coloredBigMap(levelNumber);
        };
    }

    private List<WorldMap> createMaps(String pattern, int count) {
        ArrayList<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= count; ++num) {
            String path = pattern.formatted(num);
            URL url = getClass().getResource(path);
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

    private WorldMap coloredArcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> coloredMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> coloredMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> coloredMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> coloredMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> coloredMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> coloredMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> coloredMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> coloredMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> coloredMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap coloredMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> coloredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> coloredMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> coloredMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> coloredMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> coloredMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> coloredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> coloredMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> coloredMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> coloredMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> coloredMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> coloredMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> coloredMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> coloredMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> coloredMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> coloredMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> coloredMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> coloredMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> coloredMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> coloredMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> coloredMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> coloredMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> coloredMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> coloredMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> coloredMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> coloredMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> coloredMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> coloredMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyColoredMap(MINI, 4);
            case 29 -> randomlyColoredMap(MINI, 5);
            case 30 -> randomlyColoredMap(MINI, 2);
            case 31 -> randomlyColoredMap(MINI, 3);
            case 32 -> coloredMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap coloredBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> coloredMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> coloredMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> coloredMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> coloredMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> coloredMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> coloredMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> coloredMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> coloredMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> coloredMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> coloredMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> coloredMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> coloredMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> coloredMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> coloredMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> coloredMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> coloredMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> coloredMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> coloredMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> coloredMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> coloredMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> coloredMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> coloredMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> coloredMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> coloredMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> coloredMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> coloredMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> coloredMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyColoredMap(BIG,  5);
            case 29 -> randomlyColoredMap(BIG,  9);
            case 30 -> randomlyColoredMap(BIG,  2);
            case 31 -> randomlyColoredMap(BIG, 10);
            case 32 -> coloredMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap coloredStrangeMap(int levelNumber) {
        WorldMap worldMap = switch (levelNumber) {
            case  1 -> coloredMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> coloredMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> coloredMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> coloredMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> coloredMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> coloredMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> coloredMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> coloredMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> coloredMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> coloredMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> coloredMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> coloredMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> coloredMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> coloredMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> coloredMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> coloredMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> coloredMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> coloredMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> coloredMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> coloredMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> coloredMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> coloredMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> coloredMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> coloredMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> coloredMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> coloredMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> coloredMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyColoredMap(MINI,     5);
            case 29 -> randomlyColoredMap(STRANGE,  8);
            case 30 -> randomlyColoredMap(MINI,     4);
            case 31 -> randomlyColoredMap(STRANGE, 11);
            case 32 -> coloredMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map such that the renderer can easily determine the map sprite
        worldMap.setConfigValue("levelNumber", levelNumber);
        return worldMap;
    }

    private WorldMap coloredMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = new WorldMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("randomColorScheme", false);
        return worldMap;
    }

    private WorldMap randomlyColoredMap(MapCategory category, int number) {
        WorldMap worldMap = coloredMap(category, number, NES_ColorScheme.random());
        worldMap.setConfigValue("randomColorScheme", true);
        return worldMap;
    }
}