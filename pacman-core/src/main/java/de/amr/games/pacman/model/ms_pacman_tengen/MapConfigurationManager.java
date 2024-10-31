/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.colorToHexFormat;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NamedMapColorScheme.*;

public class MapConfigurationManager {

    record MapConfig(int mapNumber, WorldMap worldMap, Map<String, String> colorScheme) {}

    private static final List<NamedMapColorScheme> COLOR_SCHEMES_IN_LEVEL_ORDER = List.of(
            MSC_36_15_20_PINK_RED_WHITE,       // Level 1
            MSC_21_20_28_BLUE_WHITE_YELLOW,
            MSC_16_20_15_ORANGE_WHITE_RED,
            MSC_01_38_20_BLUE_YELLOW_WHITE,
            MSC_35_28_20_PINK_YELLOW_WHITE,    // Level 5
            MSC_36_15_20_PINK_RED_WHITE,
            MSC_17_20_20_BROWN_WHITE_WHITE,
            MSC_13_20_28_VIOLET_WHITE_YELLOW,
            MSC_0F_20_28_BLACK_WHITE_YELLOW,
            MSC_0F_01_20_BLACK_BLUE_WHITE,     // Level 10
            MSC_14_25_20_VIOLET_ROSE_WHITE,
            MSC_15_20_20_RED_WHITE_WHITE,
            MSC_1B_20_20_GREEN_WHITE_WHITE,
            MSC_28_20_2A_YELLOW_WHITE_GREEN,
            MSC_1A_20_28_GREEN_WHITE_YELLOW,   // Level 15
            MSC_18_20_20_KHAKI_WHITE_WHITE,
            MSC_25_20_20_ROSE_WHITE_WHITE,
            MSC_12_20_28_BLUE_WHITE_YELLOW,
            MSC_07_20_20_BROWN_WHITE_WHITE,
            MSC_15_25_20_RED_ROSE_WHITE,       // Level 20
            MSC_0F_20_1C_BLACK_WHITE_GREEN,
            MSC_19_20_20_GREEN_WHITE_WHITE,
            MSC_0C_20_14_GREEN_WHITE_VIOLET,
            MSC_23_20_2B_VIOLET_WHITE_GREEN,
            MSC_10_20_28_GRAY_WHITE_YELLOW,    // Level 25
            MSC_03_20_20_VIOLET_WHITE_WHITE,
            MSC_04_20_20_VIOLET_WHITE_WHITE   // TODO clarify when randomization starts
            // Levels 28-32 use randomly selected schemes
    );

    private static Map<String, String> randomMapColorScheme() {
        return COLOR_SCHEMES_IN_LEVEL_ORDER.get(Globals.randomInt(0, COLOR_SCHEMES_IN_LEVEL_ORDER.size())).get();
    }

    static final String MAPS_ROOT = "/de/amr/games/pacman/maps/tengen/";

    static final String ARCADE_MAP_PATTERN = MAPS_ROOT + "arcade%d.world";
    static final int ARCADE_MAP_COUNT = 4;

    static final String MINI_MAP_PATTERN = MAPS_ROOT + "mini%d.world";
    static final int MINI_MAP_COUNT = 6;

    static final String STRANGE_OR_BIG_MAP_PATTERN = MAPS_ROOT + "strange_or_big/map%02d.world";
    static final int STRANGE_OR_BIG_MAP_COUNT = 33;

    private List<WorldMap> arcadeMaps;
    private List<WorldMap> miniMaps;
    private List<WorldMap> strangeOrBigMaps; //TODO separate?

    MapConfig getMapConfig(MapCategory mapCategory, int levelNumber) {
        return switch (mapCategory) {
            case ARCADE  -> selectArcadeMapForLevel(levelNumber);
            case STRANGE -> selectStrangeMapForLevel(levelNumber);
            case MINI    -> selectMiniMapForLevel(levelNumber);
            case BIG     -> selectBigMapForLevel(levelNumber);
        };
    }

    private MapConfig selectArcadeMapForLevel(int levelNumber) {
        List<WorldMap> maps = getArcadeMaps();
        return switch (levelNumber) {
            case 1,2         -> createMapConfig(maps, 1, MSC_36_15_20_PINK_RED_WHITE.get());
            case 3,4,5       -> createMapConfig(maps, 2, MSC_21_20_28_BLUE_WHITE_YELLOW.get());
            case 6,7,8,9     -> createMapConfig(maps, 3, MSC_16_20_15_ORANGE_WHITE_RED.get());
            case 10,11,12,13 -> createMapConfig(maps, 4, MSC_01_38_20_BLUE_YELLOW_WHITE.get());
            case 14,15,16,17 -> createMapConfig(maps, 3, MSC_35_28_20_PINK_YELLOW_WHITE.get());
            case 18,19,20,21 -> createMapConfig(maps, 4, MSC_36_15_20_PINK_RED_WHITE.get());
            case 22,23,24,25 -> createMapConfig(maps, 3, MSC_17_20_20_BROWN_WHITE_WHITE.get());
            //TODO also random color schemes from level 28 on?
            case 26,27       -> createMapConfig(maps, 4, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 28,29       -> createMapConfig(maps, 4, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 30,31,32    -> createMapConfig(maps, 3, MSC_0F_20_28_BLACK_WHITE_YELLOW.get());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private MapConfig selectMiniMapForLevel(int levelNumber) {
        List<WorldMap> maps = getMiniMaps();
        return switch (levelNumber) {
            case 1  -> createMapConfig(maps, 1, MSC_36_15_20_PINK_RED_WHITE.get());
            case 2  -> createMapConfig(maps, 2, MSC_21_20_28_BLUE_WHITE_YELLOW.get());
            case 3  -> createMapConfig(maps, 1, MSC_16_20_15_ORANGE_WHITE_RED.get());
            case 4  -> createMapConfig(maps, 2, MSC_01_38_20_BLUE_YELLOW_WHITE.get());
            case 5  -> createMapConfig(maps, 3, MSC_35_28_20_PINK_YELLOW_WHITE.get());
            case 6  -> createMapConfig(maps, 1, MSC_36_15_20_PINK_RED_WHITE.get());
            case 7  -> createMapConfig(maps, 2, MSC_17_20_20_BROWN_WHITE_WHITE.get());
            case 8  -> createMapConfig(maps, 3, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 9  -> createMapConfig(maps, 4, MSC_0F_20_28_BLACK_WHITE_YELLOW.get());
            case 10 -> createMapConfig(maps, 1, MSC_0F_01_20_BLACK_BLUE_WHITE.get());
            case 11 -> createMapConfig(maps, 2, MSC_14_25_20_VIOLET_ROSE_WHITE.get());
            case 12 -> createMapConfig(maps, 3, MSC_15_20_20_RED_WHITE_WHITE.get());
            case 13 -> createMapConfig(maps, 4, MSC_1B_20_20_GREEN_WHITE_WHITE.get());
            case 14 -> createMapConfig(maps, 1, MSC_28_20_2A_YELLOW_WHITE_GREEN.get());
            case 15 -> createMapConfig(maps, 2, MSC_1A_20_28_GREEN_WHITE_YELLOW.get());
            case 16 -> createMapConfig(maps, 3, MSC_18_20_20_KHAKI_WHITE_WHITE.get());
            case 17 -> createMapConfig(maps, 4, MSC_25_20_20_ROSE_WHITE_WHITE.get());
            case 18 -> createMapConfig(maps, 5, MSC_12_20_28_BLUE_WHITE_YELLOW.get());
            case 19 -> createMapConfig(maps, 5, MSC_07_20_20_BROWN_WHITE_WHITE.get());
            case 20 -> createMapConfig(maps, 4, MSC_15_25_20_RED_ROSE_WHITE.get());
            case 21 -> createMapConfig(maps, 3, MSC_0F_20_1C_BLACK_WHITE_GREEN.get());
            case 22 -> createMapConfig(maps, 2, MSC_19_20_20_GREEN_WHITE_WHITE.get());
            case 23 -> createMapConfig(maps, 1, MSC_0C_20_14_GREEN_WHITE_VIOLET.get());
            case 24 -> createMapConfig(maps, 6, MSC_23_20_2B_VIOLET_WHITE_GREEN.get());
            case 25 -> createMapConfig(maps, 1, MSC_10_20_28_GRAY_WHITE_YELLOW.get());
            case 26 -> createMapConfig(maps, 2, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 27 -> createMapConfig(maps, 3, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 28 -> createMapConfig(maps, 4, randomMapColorScheme());
            case 29 -> createMapConfig(maps, 5, randomMapColorScheme());
            case 30 -> createMapConfig(maps, 2, randomMapColorScheme());
            case 31 -> createMapConfig(maps, 3, randomMapColorScheme());
            case 32 -> createMapConfig(maps, 6, randomMapColorScheme());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private MapConfig selectBigMapForLevel(int levelNumber) {
        List<WorldMap> maps = getStrangeOrBigMaps();
        return switch (levelNumber) {
            case 1  -> createMapConfig(maps, 19, MSC_36_15_20_PINK_RED_WHITE.get());
            case 2  -> createMapConfig(maps, 20, MSC_21_20_28_BLUE_WHITE_YELLOW.get());
            case 3  -> createMapConfig(maps, 21, MSC_16_20_15_ORANGE_WHITE_RED.get());
            case 4  -> createMapConfig(maps, 19, MSC_01_38_20_BLUE_YELLOW_WHITE.get());
            case 5  -> createMapConfig(maps, 20, MSC_35_28_20_PINK_YELLOW_WHITE.get());
            case 6  -> createMapConfig(maps, 21, MSC_36_15_20_PINK_RED_WHITE.get());
            case 7  -> createMapConfig(maps, 22, MSC_17_20_20_BROWN_WHITE_WHITE.get());
            case 8  -> createMapConfig(maps, 23, MSC_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 9  -> createMapConfig(maps, 17, MSC_0F_20_28_BLACK_WHITE_YELLOW.get());
            case 10 -> createMapConfig(maps, 10, MSC_0F_01_20_BLACK_BLUE_WHITE.get());
            case 11 -> createMapConfig(maps, 23, MSC_15_25_20_RED_ROSE_WHITE.get());
            case 12 -> createMapConfig(maps, 21, MSC_25_20_20_ROSE_WHITE_WHITE.get());
            case 13 -> createMapConfig(maps, 22, MSC_1B_20_20_GREEN_WHITE_WHITE.get());
            case 14 -> createMapConfig(maps, 14, MSC_28_20_2A_YELLOW_WHITE_GREEN.get());
            case 15 -> createMapConfig(maps, 20, MSC_1A_20_28_GREEN_WHITE_YELLOW.get());
            case 16 -> createMapConfig(maps, 19, MSC_18_20_20_KHAKI_WHITE_WHITE.get());
            case 17 -> createMapConfig(maps, 10, MSC_25_20_20_ROSE_WHITE_WHITE.get());
            case 18 -> createMapConfig(maps, 17, MSC_12_20_28_BLUE_WHITE_YELLOW.get());
            case 19 -> createMapConfig(maps, 10, MSC_07_20_20_BROWN_WHITE_WHITE.get());
            case 20 -> createMapConfig(maps, 19, MSC_15_25_20_RED_ROSE_WHITE.get());
            case 21 -> createMapConfig(maps, 26, MSC_0F_20_1C_BLACK_WHITE_GREEN.get());
            case 22 -> createMapConfig(maps, 21, MSC_19_20_20_GREEN_WHITE_WHITE.get());
            case 23 -> createMapConfig(maps, 22, MSC_0C_20_14_GREEN_WHITE_VIOLET.get());
            case 24 -> createMapConfig(maps, 23, MSC_23_20_2B_VIOLET_WHITE_GREEN.get());
            case 25 -> createMapConfig(maps, 14, MSC_10_20_28_GRAY_WHITE_YELLOW.get());
            case 26 -> createMapConfig(maps, 25, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 27 -> createMapConfig(maps, 14, MSC_04_20_20_VIOLET_WHITE_WHITE.get());
            case 28 -> createMapConfig(maps, 23, randomMapColorScheme());
            case 29 -> createMapConfig(maps, 26, randomMapColorScheme());
            case 30 -> createMapConfig(maps, 20, randomMapColorScheme());
            case 31 -> createMapConfig(maps, 25, randomMapColorScheme());
            case 32 -> createMapConfig(maps, 33, randomMapColorScheme());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private MapConfig createMapConfig(List<WorldMap> maps, int mapNumber, Map<String, String> colorScheme) {
        return new MapConfig(mapNumber, new WorldMap(maps.get(mapNumber - 1)), colorScheme);
    }

    private MapConfig selectStrangeMapForLevel(int levelNumber) {
        List<WorldMap> maps = getStrangeOrBigMaps();
        int mapNumber = levelNumber; // TODO: check this
        WorldMap worldMap = maps.get(mapNumber - 1);
        return new MapConfig(mapNumber, worldMap, createColorSchemeFromMap(worldMap));
    }

    private Map<String, String> createColorSchemeFromMap(WorldMap worldMap) {
        Map<String, String> defaultScheme = MSC_36_15_20_PINK_RED_WHITE.get();
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

    private List<WorldMap> getArcadeMaps() {
        if (arcadeMaps == null) {
            Logger.info("Loading ARCADE maps");
            arcadeMaps = createMaps(ARCADE_MAP_PATTERN, ARCADE_MAP_COUNT);
            Logger.info("Maps loaded.");
        }
        return arcadeMaps;
    }

    private List<WorldMap> getMiniMaps() {
        if (miniMaps == null) {
            Logger.info("Loading MINI maps");
            miniMaps = createMaps(MINI_MAP_PATTERN, MINI_MAP_COUNT);
            Logger.info("Maps loaded.");
        }
        return miniMaps;
    }

    private List<WorldMap> getStrangeOrBigMaps() {
        if (strangeOrBigMaps == null) {
            Logger.info("Loading STRANGE and BIG maps");
            strangeOrBigMaps = createMaps(STRANGE_OR_BIG_MAP_PATTERN, STRANGE_OR_BIG_MAP_COUNT);
            Logger.info("Maps loaded.");
        }
        return strangeOrBigMaps;
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
