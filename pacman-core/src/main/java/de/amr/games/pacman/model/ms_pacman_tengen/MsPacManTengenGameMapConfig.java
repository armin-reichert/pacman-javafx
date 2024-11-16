/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.colorToHexFormat;
import static de.amr.games.pacman.lib.Globals.inRange;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.MapCategory.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;

public class MsPacManTengenGameMapConfig {

    private static final String MAPS_ROOT = "/de/amr/games/pacman/maps/ms_pacman_tengen/";

    public static final Map<String, String> HIGHLIGHT_COLOR_SCHEME = Map.of(
        "fill",   NES.Palette.color(0x0f),
        "stroke", NES.Palette.color(0x20),
        "door",   NES.Palette.color(0x0f),
        "food",   NES.Palette.color(0x0f)
    );

    private static final List<NES_ColorScheme> NES_COLOR_SCHEMES_IN_LEVEL_ORDER = List.of(
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
        MCS_04_20_20_VIOLET_WHITE_WHITE   // TODO clarify at which level exactly the randomization starts
        // Levels 28-31 use randomly selected schemes
        // Level 32 ?
    );

    public static EnumMap<NES_ColorScheme, Map<String, String>> COLOR_MAPS_OF_NES_COLOR_SCHEMES = new EnumMap<>(NES_ColorScheme.class);
    static {
        for (var nesColorScheme : NES_ColorScheme.values()) {
            COLOR_MAPS_OF_NES_COLOR_SCHEMES.put(nesColorScheme, createColorMap(nesColorScheme));
        }
    }

    private static Map<String, String> createColorMap(NES_ColorScheme nesColorScheme) {
        return Map.of(
            "fill", nesColorScheme.fillColor(),
            "stroke", nesColorScheme.strokeColor(),
            "door", nesColorScheme.strokeColor(),
            "pellet", nesColorScheme.pelletColor()
        );
    }

    public static NES_ColorScheme random_NES_colorScheme() {
        return NES_COLOR_SCHEMES_IN_LEVEL_ORDER.get(Globals.randomInt(0, NES_COLOR_SCHEMES_IN_LEVEL_ORDER.size()));
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

    public Map<String, Object> getMapConfig(MapCategory mapCategory, int levelNumber) {
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

    private Map<String, Object> arcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> createMapConfig(ARCADE, arcadeMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> createMapConfig(ARCADE, arcadeMaps, 2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> createMapConfig(ARCADE, arcadeMaps, 3, MCS_16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> createMapConfig(ARCADE, arcadeMaps, 4, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> createMapConfig(ARCADE, arcadeMaps, 3, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> createMapConfig(ARCADE, arcadeMaps, 4, MCS_36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> createMapConfig(ARCADE, arcadeMaps, 3, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> createMapConfig(ARCADE, arcadeMaps, 4, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> createMapConfig(ARCADE, arcadeMaps, 3, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private Map<String, Object> miniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createMapConfig(MINI, miniMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 2  -> createMapConfig(MINI, miniMaps, 2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createMapConfig(MINI, miniMaps, 1, MCS_16_20_15_ORANGE_WHITE_RED);
            case 4  -> createMapConfig(MINI, miniMaps, 2, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createMapConfig(MINI, miniMaps, 3, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createMapConfig(MINI, miniMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 7  -> createMapConfig(MINI, miniMaps, 2, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createMapConfig(MINI, miniMaps, 3, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createMapConfig(MINI, miniMaps, 4, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createMapConfig(MINI, miniMaps, 1, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createMapConfig(MINI, miniMaps, 2, MCS_14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createMapConfig(MINI, miniMaps, 3, MCS_15_20_20_RED_WHITE_WHITE);
            case 13 -> createMapConfig(MINI, miniMaps, 4, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createMapConfig(MINI, miniMaps, 1, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createMapConfig(MINI, miniMaps, 2, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createMapConfig(MINI, miniMaps, 3, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createMapConfig(MINI, miniMaps, 4, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createMapConfig(MINI, miniMaps, 5, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createMapConfig(MINI, miniMaps, 5, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createMapConfig(MINI, miniMaps, 4, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> createMapConfig(MINI, miniMaps, 3, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createMapConfig(MINI, miniMaps, 2, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createMapConfig(MINI, miniMaps, 1, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createMapConfig(MINI, miniMaps, 6, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createMapConfig(MINI, miniMaps, 1, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createMapConfig(MINI, miniMaps, 2, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> createMapConfig(MINI, miniMaps, 3, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createMapConfig(MINI, miniMaps, 4, random_NES_colorScheme());
            case 29 -> createMapConfig(MINI, miniMaps, 5, random_NES_colorScheme());
            case 30 -> createMapConfig(MINI, miniMaps, 2, random_NES_colorScheme());
            case 31 -> createMapConfig(MINI, miniMaps, 3, random_NES_colorScheme());
            case 32 -> createMapConfig(MINI, miniMaps, 6, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private Map<String, Object> bigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createMapConfig(BIG, bigMaps,  1, MCS_36_15_20_PINK_RED_WHITE);
            case 2  -> createMapConfig(BIG, bigMaps,  2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createMapConfig(BIG, bigMaps,  3, MCS_16_20_15_ORANGE_WHITE_RED);
            case 4  -> createMapConfig(BIG, bigMaps,  1, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createMapConfig(BIG, bigMaps,  2, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createMapConfig(BIG, bigMaps,  3, MCS_36_15_20_PINK_RED_WHITE);
            case 7  -> createMapConfig(BIG, bigMaps,  4, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createMapConfig(BIG, bigMaps,  5, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createMapConfig(BIG, bigMaps,  6, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createMapConfig(BIG, bigMaps,  7, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createMapConfig(BIG, bigMaps,  5, MCS_15_25_20_RED_ROSE_WHITE);
            case 12 -> createMapConfig(BIG, bigMaps,  3, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 13 -> createMapConfig(BIG, bigMaps,  4, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createMapConfig(BIG, bigMaps,  8, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createMapConfig(BIG, bigMaps,  2, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createMapConfig(BIG, bigMaps,  1, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createMapConfig(BIG, bigMaps,  7, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createMapConfig(BIG, bigMaps,  6, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createMapConfig(BIG, bigMaps,  7, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createMapConfig(BIG, bigMaps,  1, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> createMapConfig(BIG, bigMaps,  9, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createMapConfig(BIG, bigMaps,  3, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createMapConfig(BIG, bigMaps,  4, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createMapConfig(BIG, bigMaps,  5, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createMapConfig(BIG, bigMaps,  8, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createMapConfig(BIG, bigMaps, 10, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> createMapConfig(BIG, bigMaps,  8, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createMapConfig(BIG, bigMaps,  5, random_NES_colorScheme());
            case 29 -> createMapConfig(BIG, bigMaps,  9, random_NES_colorScheme());
            case 30 -> createMapConfig(BIG, bigMaps,  2, random_NES_colorScheme());
            case 31 -> createMapConfig(BIG, bigMaps, 10, random_NES_colorScheme());
            case 32 -> createMapConfig(BIG, bigMaps, 11, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private Map<String, Object> strangeMap(int levelNumber) {
        Map<String, Object> mapConfig = switch (levelNumber) {
            case  1 -> createMapConfig(STRANGE, strangeMaps,  1, MCS_36_15_20_PINK_RED_WHITE);
            case  2 -> createMapConfig(STRANGE, strangeMaps,  2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> createMapConfig(STRANGE, strangeMaps,  3, MCS_16_20_15_ORANGE_WHITE_RED);
            case  4 -> createMapConfig(STRANGE, strangeMaps,  4, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> createMapConfig(STRANGE, strangeMaps,  5, MCS_35_28_20_PINK_YELLOW_WHITE);
            case  6 -> createMapConfig(STRANGE, strangeMaps,  6, MCS_36_15_20_PINK_RED_WHITE);
            case  7 -> createMapConfig(STRANGE, strangeMaps,  7, MCS_17_20_20_BROWN_WHITE_WHITE);
            case  8 -> createMapConfig(STRANGE, strangeMaps,  8, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> createMapConfig(STRANGE, strangeMaps,  9, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createMapConfig(BIG,     bigMaps,      7, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createMapConfig(STRANGE, strangeMaps, 10, MCS_14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createMapConfig(STRANGE, strangeMaps, 11, MCS_15_20_20_RED_WHITE_WHITE);
            case 13 -> createMapConfig(STRANGE, strangeMaps,  6, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createMapConfig(BIG,     bigMaps,      8, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createMapConfig(STRANGE, strangeMaps, 12, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createMapConfig(MINI,    miniMaps,     5, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createMapConfig(BIG,     bigMaps,      6, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createMapConfig(STRANGE, strangeMaps, 13, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createMapConfig(BIG,     bigMaps,      1, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createMapConfig(BIG,     bigMaps,      2, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> createMapConfig(BIG,     bigMaps,      3, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createMapConfig(BIG,     bigMaps,      4, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createMapConfig(BIG,     bigMaps,      5, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createMapConfig(STRANGE, strangeMaps,  4, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createMapConfig(BIG,     bigMaps,     10, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createMapConfig(BIG,     bigMaps,      9, MCS_03_20_20_VIOLET_WHITE_WHITE);
            case 27 -> createMapConfig(STRANGE, strangeMaps, 14, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createMapConfig(MINI,    miniMaps,     5, random_NES_colorScheme());
            case 29 -> createMapConfig(STRANGE, strangeMaps,  8, random_NES_colorScheme());
            case 30 -> createMapConfig(MINI,    miniMaps,     4, random_NES_colorScheme());
            case 31 -> createMapConfig(STRANGE, strangeMaps, 12, random_NES_colorScheme());
            case 32 -> createMapConfig(STRANGE, strangeMaps, 15, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map such that the renderer can very easily determine the map sprite
        WorldMap worldMap = (WorldMap) mapConfig.get("worldMap");
        worldMap.terrain().setProperty("levelNumber", String.valueOf(levelNumber));
        return mapConfig;
    }

    private Map<String, Object> createMapConfig(MapCategory mapCategory, List<WorldMap> maps, int mapNumber, NES_ColorScheme nesColorScheme) {
        WorldMap worldMap = new WorldMap(maps.get(mapNumber - 1));
        return Map.of(
            "mapCategory", mapCategory,
            "mapNumber", mapNumber,
            "worldMap", worldMap,
            "nesColorScheme", nesColorScheme
        );
        //return new MapConfig(mapCategory, mapNumber, worldMap, nesColorScheme);
    }

    private Map<String, String> createColorSchemeFromMap(WorldMap worldMap) {
        Map<String, String> defaultColorMap = COLOR_MAPS_OF_NES_COLOR_SCHEMES.get(MCS_36_15_20_PINK_RED_WHITE);
        String fill   = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_WALL_FILL, defaultColorMap.get("fill"));
        String stroke = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_WALL_STROKE, defaultColorMap.get("stroke"));
        String door   = worldMap.terrain().getPropertyOrDefault(PROPERTY_COLOR_DOOR, defaultColorMap.get("door"));
        String pellet = worldMap.food().getPropertyOrDefault(PROPERTY_COLOR_FOOD, defaultColorMap.get("pellet"));
        return Map.of(
                "fill",   colorToHexFormat(fill).orElse(defaultColorMap.get("fill")),
                "stroke", colorToHexFormat(stroke).orElse(defaultColorMap.get("stroke")),
                "door",   colorToHexFormat(door).orElse(defaultColorMap.get("door")),
                "pellet", colorToHexFormat(pellet).orElse(defaultColorMap.get("pellet"))
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
