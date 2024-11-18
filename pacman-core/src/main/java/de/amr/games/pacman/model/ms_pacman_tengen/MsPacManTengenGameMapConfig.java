/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman_tengen;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.net.URL;
import java.util.*;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.MapCategory.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;

public class MsPacManTengenGameMapConfig {

    private static final String MAPS_ROOT = "/de/amr/games/pacman/maps/ms_pacman_tengen/";

    public static final Map<String, String> BLACK_WHITE_COLOR_MAP = Map.of(
        "fill",   NES.Palette.color(0x0f),
        "stroke", NES.Palette.color(0x20),
        "door",   NES.Palette.color(0x0f),
        "food",   NES.Palette.color(0x0f)
    );

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

    public static NES_ColorScheme random_NES_ColorScheme() {
        var all = NES_ColorScheme.values();
        return all[randomInt(0, all.length)];
    }

    private static List<WorldMap> createMaps(Class<?> loadingClass, String pattern, int maxNumber) {
        List<WorldMap> maps = new ArrayList<>();
        for (int num = 1; num <= maxNumber; ++num) {
            String path = pattern.formatted(num);
            URL url = loadingClass.getResource(path);
            if (url != null) {
                WorldMap worldMap = new WorldMap(url);
                maps.add(worldMap);
                Logger.info("World map #{} read. URL='{}'", num, url);
            } else {
                Logger.error("World map #{} could not be read from URL '{}'", num, url);
            }
        }
        return maps;
    }

    private List<WorldMap> arcadeMaps, miniMaps, bigMaps, strangeMaps;

    public void loadMaps() {
        arcadeMaps  = createMaps(getClass(), MAPS_ROOT + "arcade%d.world", 4);
        miniMaps    = createMaps(getClass(), MAPS_ROOT + "mini%d.world", 6);
        bigMaps     = createMaps(getClass(), MAPS_ROOT + "big%d.world", 11);
        strangeMaps = createMaps(getClass(), MAPS_ROOT + "strange%d.world", 15);
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
            case 1,2         -> cfg(ARCADE, arcadeMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> cfg(ARCADE, arcadeMaps, 2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> cfg(ARCADE, arcadeMaps, 3, MCS_16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> cfg(ARCADE, arcadeMaps, 4, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> cfg(ARCADE, arcadeMaps, 3, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> cfg(ARCADE, arcadeMaps, 4, MCS_36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> cfg(ARCADE, arcadeMaps, 3, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> cfg(ARCADE, arcadeMaps, 4, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> cfg(ARCADE, arcadeMaps, 3, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=cD0oGudVpbw">YouTube video</a>.
     */
    private Map<String, Object> miniMapConfig(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> cfg(MINI, miniMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 2  -> cfg(MINI, miniMaps, 2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> cfg(MINI, miniMaps, 1, MCS_16_20_15_ORANGE_WHITE_RED);
            case 4  -> cfg(MINI, miniMaps, 2, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> cfg(MINI, miniMaps, 3, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 6  -> cfg(MINI, miniMaps, 1, MCS_36_15_20_PINK_RED_WHITE);
            case 7  -> cfg(MINI, miniMaps, 2, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 8  -> cfg(MINI, miniMaps, 3, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> cfg(MINI, miniMaps, 4, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> cfg(MINI, miniMaps, 1, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> cfg(MINI, miniMaps, 2, MCS_14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> cfg(MINI, miniMaps, 3, MCS_15_20_20_RED_WHITE_WHITE);
            case 13 -> cfg(MINI, miniMaps, 4, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> cfg(MINI, miniMaps, 1, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> cfg(MINI, miniMaps, 2, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> cfg(MINI, miniMaps, 3, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> cfg(MINI, miniMaps, 4, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> cfg(MINI, miniMaps, 5, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> cfg(MINI, miniMaps, 5, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> cfg(MINI, miniMaps, 4, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> cfg(MINI, miniMaps, 3, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> cfg(MINI, miniMaps, 2, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> cfg(MINI, miniMaps, 1, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> cfg(MINI, miniMaps, 6, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> cfg(MINI, miniMaps, 1, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> cfg(MINI, miniMaps, 2, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> cfg(MINI, miniMaps, 3, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> cfg(MINI, miniMaps, 4, random_NES_ColorScheme());
            case 29 -> cfg(MINI, miniMaps, 5, random_NES_ColorScheme());
            case 30 -> cfg(MINI, miniMaps, 2, random_NES_ColorScheme());
            case 31 -> cfg(MINI, miniMaps, 3, random_NES_ColorScheme());
            case 32 -> cfg(MINI, miniMaps, 6, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    /**
     * From this <a href="https://www.youtube.com/watch?v=NoImGoSAL7A">YouTube video</a>.
     */
    private Map<String, Object> bigMapConfig(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> cfg(BIG, bigMaps,  1, MCS_36_15_20_PINK_RED_WHITE);
            case 2  -> cfg(BIG, bigMaps,  2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> cfg(BIG, bigMaps,  3, MCS_16_20_15_ORANGE_WHITE_RED);
            case 4  -> cfg(BIG, bigMaps,  1, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> cfg(BIG, bigMaps,  2, MCS_35_28_20_PINK_YELLOW_WHITE);
            case 6  -> cfg(BIG, bigMaps,  3, MCS_36_15_20_PINK_RED_WHITE);
            case 7  -> cfg(BIG, bigMaps,  4, MCS_17_20_20_BROWN_WHITE_WHITE);
            case 8  -> cfg(BIG, bigMaps,  5, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> cfg(BIG, bigMaps,  6, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> cfg(BIG, bigMaps,  7, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> cfg(BIG, bigMaps,  5, MCS_15_25_20_RED_ROSE_WHITE);
            case 12 -> cfg(BIG, bigMaps,  3, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 13 -> cfg(BIG, bigMaps,  4, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> cfg(BIG, bigMaps,  8, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> cfg(BIG, bigMaps,  2, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> cfg(BIG, bigMaps,  1, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> cfg(BIG, bigMaps,  7, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> cfg(BIG, bigMaps,  6, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> cfg(BIG, bigMaps,  7, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> cfg(BIG, bigMaps,  1, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> cfg(BIG, bigMaps,  9, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> cfg(BIG, bigMaps,  3, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> cfg(BIG, bigMaps,  4, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> cfg(BIG, bigMaps,  5, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> cfg(BIG, bigMaps,  8, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> cfg(BIG, bigMaps, 10, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 27 -> cfg(BIG, bigMaps,  8, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> cfg(BIG, bigMaps,  5, random_NES_ColorScheme());
            case 29 -> cfg(BIG, bigMaps,  9, random_NES_ColorScheme());
            case 30 -> cfg(BIG, bigMaps,  2, random_NES_ColorScheme());
            case 31 -> cfg(BIG, bigMaps, 10, random_NES_ColorScheme());
            case 32 -> cfg(BIG, bigMaps, 11, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private Map<String, Object> strangeMapConfig(int levelNumber) {
        Map<String, Object> mapConfig = switch (levelNumber) {
            case  1 -> cfg(STRANGE, strangeMaps,  1, MCS_36_15_20_PINK_RED_WHITE);
            case  2 -> cfg(STRANGE, strangeMaps,  2, MCS_21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> cfg(STRANGE, strangeMaps,  3, MCS_16_20_15_ORANGE_WHITE_RED);
            case  4 -> cfg(STRANGE, strangeMaps,  4, MCS_01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> cfg(STRANGE, strangeMaps,  5, MCS_35_28_20_PINK_YELLOW_WHITE);
            case  6 -> cfg(STRANGE, strangeMaps,  6, MCS_36_15_20_PINK_RED_WHITE);
            case  7 -> cfg(STRANGE, strangeMaps,  7, MCS_17_20_20_BROWN_WHITE_WHITE);
            case  8 -> cfg(STRANGE, strangeMaps,  8, MCS_13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> cfg(STRANGE, strangeMaps,  9, MCS_0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> cfg(BIG,     bigMaps,      7, MCS_0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> cfg(STRANGE, strangeMaps, 10, MCS_14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> cfg(STRANGE, strangeMaps, 11, MCS_15_20_20_RED_WHITE_WHITE);
            case 13 -> cfg(STRANGE, strangeMaps,  6, MCS_1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> cfg(BIG,     bigMaps,      8, MCS_28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> cfg(STRANGE, strangeMaps, 12, MCS_1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> cfg(MINI,    miniMaps,     5, MCS_18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> cfg(BIG,     bigMaps,      6, MCS_25_20_20_ROSE_WHITE_WHITE);
            case 18 -> cfg(STRANGE, strangeMaps, 13, MCS_12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> cfg(BIG,     bigMaps,      1, MCS_07_20_20_BROWN_WHITE_WHITE);
            case 20 -> cfg(BIG,     bigMaps,      2, MCS_15_25_20_RED_ROSE_WHITE);
            case 21 -> cfg(BIG,     bigMaps,      3, MCS_0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> cfg(BIG,     bigMaps,      4, MCS_19_20_20_GREEN_WHITE_WHITE);
            case 23 -> cfg(BIG,     bigMaps,      5, MCS_0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> cfg(STRANGE, strangeMaps,  4, MCS_23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> cfg(BIG,     bigMaps,     10, MCS_10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> cfg(BIG,     bigMaps,      9, MCS_03_20_20_VIOLET_WHITE_WHITE);
            case 27 -> cfg(STRANGE, strangeMaps, 14, MCS_04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> cfg(MINI,    miniMaps,     5, random_NES_ColorScheme());
            case 29 -> cfg(STRANGE, strangeMaps,  8, random_NES_ColorScheme());
            case 30 -> cfg(MINI,    miniMaps,     4, random_NES_ColorScheme());
            case 31 -> cfg(STRANGE, strangeMaps, 12, random_NES_ColorScheme());
            case 32 -> cfg(STRANGE, strangeMaps, 15, MCS_15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        // TODO: Hack: Store level number in map config such that the renderer can easily determine the map sprite
        Map<String, Object> extConfig = new HashMap<>(mapConfig);
        extConfig.put("levelNumber", levelNumber);
        return extConfig;
    }

    private Map<String, Object> cfg(MapCategory mapCategory, List<WorldMap> maps, int mapNumber, NES_ColorScheme nesColorScheme) {
        WorldMap worldMap = new WorldMap(maps.get(mapNumber - 1));
        return Map.of(
            "mapCategory", mapCategory,
            "mapNumber", mapNumber,
            "worldMap", worldMap,
            "nesColorScheme", nesColorScheme
        );
    }

    private Map<String, String> createColorSchemeFromMap(WorldMap worldMap) {
        Map<String, String> defaultColorMap = COLOR_MAPS.get(MCS_36_15_20_PINK_RED_WHITE);
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
}