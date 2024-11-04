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
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NamedMapColorScheme.*;

public class MapConfigurationManager {

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

    public static Map<String, String> randomMapColorScheme() {
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
        List<WorldMap> arcade = getArcadeMaps();
        return switch (levelNumber) {
            case 1,2         -> createMapConfig(arcade, 1, MCS_36_15_20_PINK_RED_WHITE.get());
            case 3,4,5       -> createMapConfig(arcade, 2, MCS_21_20_28_BLUE_WHITE_YELLOW.get());
            case 6,7,8,9     -> createMapConfig(arcade, 3, MCS_16_20_15_ORANGE_WHITE_RED.get());
            case 10,11,12,13 -> createMapConfig(arcade, 4, MCS_01_38_20_BLUE_YELLOW_WHITE.get());
            case 14,15,16,17 -> createMapConfig(arcade, 3, MCS_35_28_20_PINK_YELLOW_WHITE.get());
            case 18,19,20,21 -> createMapConfig(arcade, 4, MCS_36_15_20_PINK_RED_WHITE.get());
            case 22,23,24,25 -> createMapConfig(arcade, 3, MCS_17_20_20_BROWN_WHITE_WHITE.get());
            case 26,27,28,29 -> createMapConfig(arcade, 4, MCS_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 30,31,32    -> createMapConfig(arcade, 3, MCS_0F_20_28_BLACK_WHITE_YELLOW.get());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private MapConfig selectMiniMapForLevel(int levelNumber) {
        List<WorldMap> mini = getMiniMaps();
        return switch (levelNumber) {
            case 1  -> createMapConfig(mini, 1, MCS_36_15_20_PINK_RED_WHITE.get());
            case 2  -> createMapConfig(mini, 2, MCS_21_20_28_BLUE_WHITE_YELLOW.get());
            case 3  -> createMapConfig(mini, 1, MCS_16_20_15_ORANGE_WHITE_RED.get());
            case 4  -> createMapConfig(mini, 2, MCS_01_38_20_BLUE_YELLOW_WHITE.get());
            case 5  -> createMapConfig(mini, 3, MCS_35_28_20_PINK_YELLOW_WHITE.get());
            case 6  -> createMapConfig(mini, 1, MCS_36_15_20_PINK_RED_WHITE.get());
            case 7  -> createMapConfig(mini, 2, MCS_17_20_20_BROWN_WHITE_WHITE.get());
            case 8  -> createMapConfig(mini, 3, MCS_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 9  -> createMapConfig(mini, 4, MCS_0F_20_28_BLACK_WHITE_YELLOW.get());
            case 10 -> createMapConfig(mini, 1, MCS_0F_01_20_BLACK_BLUE_WHITE.get());
            case 11 -> createMapConfig(mini, 2, MCS_14_25_20_VIOLET_ROSE_WHITE.get());
            case 12 -> createMapConfig(mini, 3, MCS_15_20_20_RED_WHITE_WHITE.get());
            case 13 -> createMapConfig(mini, 4, MCS_1B_20_20_GREEN_WHITE_WHITE.get());
            case 14 -> createMapConfig(mini, 1, MCS_28_20_2A_YELLOW_WHITE_GREEN.get());
            case 15 -> createMapConfig(mini, 2, MCS_1A_20_28_GREEN_WHITE_YELLOW.get());
            case 16 -> createMapConfig(mini, 3, MCS_18_20_20_KHAKI_WHITE_WHITE.get());
            case 17 -> createMapConfig(mini, 4, MCS_25_20_20_ROSE_WHITE_WHITE.get());
            case 18 -> createMapConfig(mini, 5, MCS_12_20_28_BLUE_WHITE_YELLOW.get());
            case 19 -> createMapConfig(mini, 5, MCS_07_20_20_BROWN_WHITE_WHITE.get());
            case 20 -> createMapConfig(mini, 4, MCS_15_25_20_RED_ROSE_WHITE.get());
            case 21 -> createMapConfig(mini, 3, MCS_0F_20_1C_BLACK_WHITE_GREEN.get());
            case 22 -> createMapConfig(mini, 2, MCS_19_20_20_GREEN_WHITE_WHITE.get());
            case 23 -> createMapConfig(mini, 1, MCS_0C_20_14_GREEN_WHITE_VIOLET.get());
            case 24 -> createMapConfig(mini, 6, MCS_23_20_2B_VIOLET_WHITE_GREEN.get());
            case 25 -> createMapConfig(mini, 1, MCS_10_20_28_GRAY_WHITE_YELLOW.get());
            case 26 -> createMapConfig(mini, 2, MCS_04_20_20_VIOLET_WHITE_WHITE.get());
            case 27 -> createMapConfig(mini, 3, MCS_04_20_20_VIOLET_WHITE_WHITE.get());
            case 28 -> createMapConfig(mini, 4, randomMapColorScheme());
            case 29 -> createMapConfig(mini, 5, randomMapColorScheme());
            case 30 -> createMapConfig(mini, 2, randomMapColorScheme());
            case 31 -> createMapConfig(mini, 3, randomMapColorScheme());
            case 32 -> createMapConfig(mini, 6, MCS_15_25_20_RED_ROSE_WHITE.get());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private MapConfig selectBigMapForLevel(int levelNumber) {
        List<WorldMap> big = getStrangeOrBigMaps();
        return switch (levelNumber) {
            case 1  -> createMapConfig(big, 19, MCS_36_15_20_PINK_RED_WHITE.get());
            case 2  -> createMapConfig(big, 20, MCS_21_20_28_BLUE_WHITE_YELLOW.get());
            case 3  -> createMapConfig(big, 21, MCS_16_20_15_ORANGE_WHITE_RED.get());
            case 4  -> createMapConfig(big, 19, MCS_01_38_20_BLUE_YELLOW_WHITE.get());
            case 5  -> createMapConfig(big, 20, MCS_35_28_20_PINK_YELLOW_WHITE.get());
            case 6  -> createMapConfig(big, 21, MCS_36_15_20_PINK_RED_WHITE.get());
            case 7  -> createMapConfig(big, 22, MCS_17_20_20_BROWN_WHITE_WHITE.get());
            case 8  -> createMapConfig(big, 23, MCS_13_20_28_VIOLET_WHITE_YELLOW.get());
            case 9  -> createMapConfig(big, 17, MCS_0F_20_28_BLACK_WHITE_YELLOW.get());
            case 10 -> createMapConfig(big, 10, MCS_0F_01_20_BLACK_BLUE_WHITE.get());
            case 11 -> createMapConfig(big, 23, MCS_15_25_20_RED_ROSE_WHITE.get());
            case 12 -> createMapConfig(big, 21, MCS_25_20_20_ROSE_WHITE_WHITE.get());
            case 13 -> createMapConfig(big, 22, MCS_1B_20_20_GREEN_WHITE_WHITE.get());
            case 14 -> createMapConfig(big, 14, MCS_28_20_2A_YELLOW_WHITE_GREEN.get());
            case 15 -> createMapConfig(big, 20, MCS_1A_20_28_GREEN_WHITE_YELLOW.get());
            case 16 -> createMapConfig(big, 19, MCS_18_20_20_KHAKI_WHITE_WHITE.get());
            case 17 -> createMapConfig(big, 10, MCS_25_20_20_ROSE_WHITE_WHITE.get());
            case 18 -> createMapConfig(big, 17, MCS_12_20_28_BLUE_WHITE_YELLOW.get());
            case 19 -> createMapConfig(big, 10, MCS_07_20_20_BROWN_WHITE_WHITE.get());
            case 20 -> createMapConfig(big, 19, MCS_15_25_20_RED_ROSE_WHITE.get());
            case 21 -> createMapConfig(big, 26, MCS_0F_20_1C_BLACK_WHITE_GREEN.get());
            case 22 -> createMapConfig(big, 21, MCS_19_20_20_GREEN_WHITE_WHITE.get());
            case 23 -> createMapConfig(big, 22, MCS_0C_20_14_GREEN_WHITE_VIOLET.get());
            case 24 -> createMapConfig(big, 23, MCS_23_20_2B_VIOLET_WHITE_GREEN.get());
            case 25 -> createMapConfig(big, 14, MCS_10_20_28_GRAY_WHITE_YELLOW.get());
            case 26 -> createMapConfig(big, 25, MCS_04_20_20_VIOLET_WHITE_WHITE.get());
            case 27 -> createMapConfig(big, 14, MCS_04_20_20_VIOLET_WHITE_WHITE.get());
            case 28 -> createMapConfig(big, 23, randomMapColorScheme());
            case 29 -> createMapConfig(big, 26, randomMapColorScheme());
            case 30 -> createMapConfig(big, 20, randomMapColorScheme());
            case 31 -> createMapConfig(big, 25, randomMapColorScheme());
            case 32 -> createMapConfig(big, 33, MCS_15_25_20_RED_ROSE_WHITE.get());
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private MapConfig selectStrangeMapForLevel(int levelNumber) {
        List<WorldMap> strange = getStrangeOrBigMaps();
        int mapNumber = levelNumber; // TODO: check this
        WorldMap worldMap = strange.get(mapNumber - 1);
        return new MapConfig(mapNumber, worldMap, createColorSchemeFromMap(worldMap));
    }

    private MapConfig createMapConfig(List<WorldMap> maps, int mapNumber, Map<String, String> colorScheme) {
        return new MapConfig(mapNumber, new WorldMap(maps.get(mapNumber - 1)), colorScheme);
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
