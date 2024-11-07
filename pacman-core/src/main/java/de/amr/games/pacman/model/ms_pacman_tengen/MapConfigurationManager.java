/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.MapConfig;
import org.tinylog.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.colorToHexFormat;
import static de.amr.games.pacman.lib.Globals.inRange;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NamedMapColorScheme.*;

public class MapConfigurationManager {

    static final String MAPS_ROOT = "/de/amr/games/pacman/maps/ms_pacman_tengen/";

    public static final Map<String, String> HIGHLIGHT_COLOR_SCHEME = Map.of(
            "fill",   NES.Palette.color(0x0f),
            "stroke", NES.Palette.color(0x20),
            "door",   NES.Palette.color(0x0f),
            "food",   NES.Palette.color(0x0f)
    );

    private static final List<NamedMapColorScheme> COLOR_SCHEMES_IN_LEVEL_ORDER = List.of(
        MCS_36_15_20_PINK_RED_WHITE,       // Level 1
        MCS_21_20_28_BLUE_WHITE_YELLOW,
        MCS_16_20_15_ORANGE_WHITE_RED,
        MCS_01_38_20_BLUE_YELLOW_WHITE,
        MCS_35_28_20_PINK_YELLOW_WHITE,    // Level 5
        MCS_36_15_20_PINK_RED_WHITE,
        MCS_17_20_20_BROWN_WHITE_WHITE,
        MCS_13_20_28_VIOLET_WHITE_YELLOW,
        MCS_0F_20_28_BLACK_WHITE_YELLOW,
        MCS_0F_01_20_BLACK_BLUE_WHITE,     // Level 10
        MCS_14_25_20_VIOLET_ROSE_WHITE,
        MCS_15_20_20_RED_WHITE_WHITE,
        MCS_1B_20_20_GREEN_WHITE_WHITE,
        MCS_28_20_2A_YELLOW_WHITE_GREEN,
        MCS_1A_20_28_GREEN_WHITE_YELLOW,   // Level 15
        MCS_18_20_20_KHAKI_WHITE_WHITE,
        MCS_25_20_20_ROSE_WHITE_WHITE,
        MCS_12_20_28_BLUE_WHITE_YELLOW,
        MCS_07_20_20_BROWN_WHITE_WHITE,
        MCS_15_25_20_RED_ROSE_WHITE,       // Level 20
        MCS_0F_20_1C_BLACK_WHITE_GREEN,
        MCS_19_20_20_GREEN_WHITE_WHITE,
        MCS_0C_20_14_GREEN_WHITE_VIOLET,
        MCS_23_20_2B_VIOLET_WHITE_GREEN,
        MCS_10_20_28_GRAY_WHITE_YELLOW,    // Level 25
        MCS_03_20_20_VIOLET_WHITE_WHITE,
        MCS_04_20_20_VIOLET_WHITE_WHITE   // TODO clarify when randomization starts
        // Levels 28-31 use randomly selected schemes
    );

    public static NamedMapColorScheme randomMapColorScheme() {
        return COLOR_SCHEMES_IN_LEVEL_ORDER.get(Globals.randomInt(0, COLOR_SCHEMES_IN_LEVEL_ORDER.size()));
    }

    private List<WorldMap> arcadeMaps;
    private List<WorldMap> miniMaps;
    private List<WorldMap> bigMaps;
    private List<WorldMap> strangeMaps;

    public void loadMaps() {
        arcadeMaps = createMaps(MAPS_ROOT + "arcade%d.world", 4);
        miniMaps = createMaps(MAPS_ROOT + "mini%d.world", 6);
        bigMaps = createMaps(MAPS_ROOT + "big%d.world", 11);
        strangeMaps = createMaps(MAPS_ROOT + "strange%d.world", 15);
    }

    public MapConfig getMapConfig(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE  -> arcadeMap(levelNumber);
            case STRANGE -> strangeMap(levelNumber);
            case MINI    -> miniMap(levelNumber);
            case BIG     -> bigMap(levelNumber);
        };
    }

    public boolean isRandomColorSchemeUsed(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE -> false; // TODO check
            case BIG, MINI -> inRange(levelNumber, 28, 31);
            case STRANGE -> false; // TODO not true
        };
    }

    private MapConfig arcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 3, MCS_16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 4, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 3, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 4, MCS_36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 3, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 4, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> createMapConfig(MapCategory.ARCADE, arcadeMaps, 3, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private MapConfig miniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 2  -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_16_20_15_ORANGE_WHITE_RED);
            case 4  -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createMapConfig(MapCategory.MINI, miniMaps, 3, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 7  -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createMapConfig(MapCategory.MINI, miniMaps, 3, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createMapConfig(MapCategory.MINI, miniMaps, 4, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createMapConfig(MapCategory.MINI, miniMaps, 3, MCS_15_20_20_RED_WHITE_WHITE);
            case 13 -> createMapConfig(MapCategory.MINI, miniMaps, 4, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createMapConfig(MapCategory.MINI, miniMaps, 3, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createMapConfig(MapCategory.MINI, miniMaps, 4, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createMapConfig(MapCategory.MINI, miniMaps, 5, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createMapConfig(MapCategory.MINI, miniMaps, 5, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createMapConfig(MapCategory.MINI, miniMaps, 4, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> createMapConfig(MapCategory.MINI, miniMaps, 3, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createMapConfig(MapCategory.MINI, miniMaps, 6, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createMapConfig(MapCategory.MINI, miniMaps, 1, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createMapConfig(MapCategory.MINI, miniMaps, 2, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> createMapConfig(MapCategory.MINI, miniMaps, 3, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createMapConfig(MapCategory.MINI, miniMaps, 4, randomMapColorScheme());
            case 29 -> createMapConfig(MapCategory.MINI, miniMaps, 5, randomMapColorScheme());
            case 30 -> createMapConfig(MapCategory.MINI, miniMaps, 2, randomMapColorScheme());
            case 31 -> createMapConfig(MapCategory.MINI, miniMaps, 3, randomMapColorScheme());
            case 32 -> createMapConfig(MapCategory.MINI, miniMaps, 6, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private MapConfig bigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createMapConfig(MapCategory.BIG, bigMaps,  1, MCS_36_15_20_PINK_RED_WHITE);
            case 2  -> createMapConfig(MapCategory.BIG, bigMaps,  2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createMapConfig(MapCategory.BIG, bigMaps,  3, MCS_16_20_15_ORANGE_WHITE_RED);
            case 4  -> createMapConfig(MapCategory.BIG, bigMaps,  1, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createMapConfig(MapCategory.BIG, bigMaps,  2, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createMapConfig(MapCategory.BIG, bigMaps,  3, MCS_36_15_20_PINK_RED_WHITE);
            case 7  -> createMapConfig(MapCategory.BIG, bigMaps,  4, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createMapConfig(MapCategory.BIG, bigMaps,  5, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createMapConfig(MapCategory.BIG, bigMaps,  6, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createMapConfig(MapCategory.BIG, bigMaps,  7, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createMapConfig(MapCategory.BIG, bigMaps,  5, MCS_15_25_20_RED_ROSE_WHITE);
            case 12 -> createMapConfig(MapCategory.BIG, bigMaps,  3, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 13 -> createMapConfig(MapCategory.BIG, bigMaps,  4, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createMapConfig(MapCategory.BIG, bigMaps,  8, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createMapConfig(MapCategory.BIG, bigMaps,  2, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createMapConfig(MapCategory.BIG, bigMaps,  1, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createMapConfig(MapCategory.BIG, bigMaps,  7, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createMapConfig(MapCategory.BIG, bigMaps,  6, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createMapConfig(MapCategory.BIG, bigMaps,  7, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createMapConfig(MapCategory.BIG, bigMaps,  1, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> createMapConfig(MapCategory.BIG, bigMaps,  9, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createMapConfig(MapCategory.BIG, bigMaps,  3, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createMapConfig(MapCategory.BIG, bigMaps,  4, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createMapConfig(MapCategory.BIG, bigMaps,  5, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createMapConfig(MapCategory.BIG, bigMaps,  8, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createMapConfig(MapCategory.BIG, bigMaps, 10, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> createMapConfig(MapCategory.BIG, bigMaps,  8, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createMapConfig(MapCategory.BIG, bigMaps,  5, randomMapColorScheme());
            case 29 -> createMapConfig(MapCategory.BIG, bigMaps,  9, randomMapColorScheme());
            case 30 -> createMapConfig(MapCategory.BIG, bigMaps,  2, randomMapColorScheme());
            case 31 -> createMapConfig(MapCategory.BIG, bigMaps, 10, randomMapColorScheme());
            case 32 -> createMapConfig(MapCategory.BIG, bigMaps, 11, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private MapConfig strangeMap(int levelNumber) {
        MapConfig mapConfig = switch (levelNumber) {
            case 1,2,3,4,5,6,7,8,9 -> createMapConfig(MapCategory.STRANGE, strangeMaps, levelNumber);
            case 10 -> createMapConfig(MapCategory.BIG, bigMaps, 7);
            case 11 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 10);
            case 12 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 11);
            case 13 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 6);
            case 14 -> createMapConfig(MapCategory.BIG, bigMaps, 8);
            case 15 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 12);
            case 16 -> createMapConfig(MapCategory.MINI, miniMaps,  5, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createMapConfig(MapCategory.BIG, bigMaps, 6);
            case 18 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 13);
            case 19 -> createMapConfig(MapCategory.BIG, bigMaps, 1);
            case 20 -> createMapConfig(MapCategory.BIG, bigMaps, 2);
            case 21 -> createMapConfig(MapCategory.BIG, bigMaps, 3);
            case 22 -> createMapConfig(MapCategory.BIG, bigMaps, 4);
            case 23 -> createMapConfig(MapCategory.BIG, bigMaps, 5);
            case 24 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 4);
            case 25 -> createMapConfig(MapCategory.BIG, bigMaps, 10);
            case 26 -> createMapConfig(MapCategory.BIG, bigMaps, 9);
            case 27 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 14);
            case 28 -> createMapConfig(MapCategory.MINI, miniMaps,  5, randomMapColorScheme());
            case 29 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 8, randomMapColorScheme());
            case 30 -> createMapConfig(MapCategory.MINI, miniMaps,  4, randomMapColorScheme());
            case 31 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 12, randomMapColorScheme());
            case 32 -> createMapConfig(MapCategory.STRANGE, strangeMaps, 15);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // Hack: put level number into map config such that renderer can very easily determine map sprite
        mapConfig.worldMap().terrain().setProperty("levelNumber", String.valueOf(levelNumber));
        return mapConfig;
    }

    private MapConfig createMapConfig(MapCategory mapCategory, List<WorldMap> maps, int mapNumber, NamedMapColorScheme colorScheme) {
        return new MapConfig(mapCategory, mapNumber, new WorldMap(maps.get(mapNumber - 1)), colorScheme.get());
    }

    private MapConfig createMapConfig(MapCategory mapCategory, List<WorldMap> maps, int mapNumber) {
        WorldMap worldMap = new WorldMap(maps.get(mapNumber - 1));
        return new MapConfig(mapCategory, mapNumber, worldMap, createColorSchemeFromMap(worldMap));
    }

    private Map<String, String> createColorSchemeFromMap(WorldMap worldMap) {
        Map<String, String> defaultScheme = MCS_36_15_20_PINK_RED_WHITE.get();
        String fill   = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_WALL_FILL, defaultScheme.get("fill"));
        String stroke = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_WALL_STROKE, defaultScheme.get("stroke"));
        String door   = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_DOOR, defaultScheme.get("door"));
        String pellet = worldMap.food().getPropertyOrDefault(PROPERTY_COLOR_FOOD, defaultScheme.get("pellet"));
        return Map.of(
                "fill",   colorToHexFormat(fill).orElse(defaultScheme.get("fill")),
                "stroke", colorToHexFormat(stroke).orElse(defaultScheme.get("stroke")),
                "door",   colorToHexFormat(door).orElse(defaultScheme.get("door")),
                "pellet", colorToHexFormat(pellet).orElse(defaultScheme.get("pellet"))
        );
    }

    private List<WorldMap> createMaps(String pattern, int maxNumber) {
        List<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= maxNumber; ++num) {
            String path = pattern.formatted(num);
            URL url = getClass().getResource(path);
            if (url != null) {
                WorldMap worldMap = new WorldMap(url);
                maps.add(worldMap);
                Logger.info("World map #{} has been read from URL {}", num, url);
            } else {
                Logger.error("World map #{} could not be read from URL {}", num, url);
            }
        }
        return maps;
    }

}
