/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapSelector;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.nes.NES_ColorScheme.*;
import static de.amr.games.pacman.tengen.ms_pacman.MapCategory.*;

public class TengenMsPacMan_MapSelector extends MapSelector {

    private static final String MAPS_ROOT = "/de/amr/games/pacman/tengen/ms_pacman/maps/";

    private final Map<MapCategory, List<WorldMap>> mapRepository = new EnumMap<>(MapCategory.class);

    public TengenMsPacMan_MapSelector() {}

    @Override
    public List<WorldMap> builtinMaps() {
        List<WorldMap> maps = new ArrayList<>();
        for (MapCategory category : MapCategory.values()) {
            maps.addAll(mapRepository.get(category));
        }
        return maps;
    }

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
            case ARCADE  -> createArcadeMap(levelNumber);
            case STRANGE -> createStrangeMap(levelNumber);
            case MINI    -> createMiniMap(levelNumber);
            case BIG     -> createBigMap(levelNumber);
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
                    maps.add(worldMap);
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
            case 1,2         -> createRecoloredMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> createRecoloredMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> createRecoloredMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> createRecoloredMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> createRecoloredMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> createRecoloredMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> createRecoloredMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> createRecoloredMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> createRecoloredMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createRecoloredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> createRecoloredMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createRecoloredMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> createRecoloredMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createRecoloredMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createRecoloredMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> createRecoloredMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createRecoloredMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createRecoloredMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createRecoloredMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createRecoloredMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createRecoloredMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createRecoloredMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createRecoloredMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createRecoloredMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createRecoloredMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createRecoloredMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createRecoloredMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createRecoloredMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createRecoloredMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createRecoloredMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createRecoloredMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createRecoloredMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createRecoloredMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createRecoloredMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createRecoloredMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createRecoloredMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredMap(MINI, 4);
            case 29 -> createRandomlyColoredMap(MINI, 5);
            case 30 -> createRandomlyColoredMap(MINI, 2);
            case 31 -> createRandomlyColoredMap(MINI, 3);
            case 32 -> createRecoloredMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createRecoloredMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> createRecoloredMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createRecoloredMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> createRecoloredMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createRecoloredMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createRecoloredMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> createRecoloredMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createRecoloredMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createRecoloredMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createRecoloredMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createRecoloredMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createRecoloredMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createRecoloredMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createRecoloredMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createRecoloredMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createRecoloredMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createRecoloredMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createRecoloredMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createRecoloredMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createRecoloredMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createRecoloredMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createRecoloredMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createRecoloredMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createRecoloredMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createRecoloredMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createRecoloredMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createRecoloredMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredMap(BIG,  5);
            case 29 -> createRandomlyColoredMap(BIG,  9);
            case 30 -> createRandomlyColoredMap(BIG,  2);
            case 31 -> createRandomlyColoredMap(BIG, 10);
            case 32 -> createRecoloredMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createStrangeMap(int levelNumber) {
        WorldMap worldMap = switch (levelNumber) {
            case  1 -> createRecoloredMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> createRecoloredMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> createRecoloredMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> createRecoloredMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> createRecoloredMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> createRecoloredMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> createRecoloredMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> createRecoloredMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> createRecoloredMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createRecoloredMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createRecoloredMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createRecoloredMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createRecoloredMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createRecoloredMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createRecoloredMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createRecoloredMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createRecoloredMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createRecoloredMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createRecoloredMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createRecoloredMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createRecoloredMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createRecoloredMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createRecoloredMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createRecoloredMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createRecoloredMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createRecoloredMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createRecoloredMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredMap(MINI,     5);
            case 29 -> createRandomlyColoredMap(STRANGE,  8);
            case 30 -> createRandomlyColoredMap(MINI,     4);
            case 31 -> createRandomlyColoredMap(STRANGE, 11);
            case 32 -> createRecoloredMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map such that the renderer can easily determine the map sprite
        worldMap.setConfigValue("levelNumber", levelNumber);
        return worldMap;
    }

    private WorldMap createRecoloredMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = new WorldMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("randomColorScheme", false);
        return worldMap;
    }

    private WorldMap createRandomlyColoredMap(MapCategory category, int number) {
        WorldMap worldMap = createRecoloredMap(category, number, NES_ColorScheme.random());
        worldMap.setConfigValue("randomColorScheme", true);
        return worldMap;
    }
}