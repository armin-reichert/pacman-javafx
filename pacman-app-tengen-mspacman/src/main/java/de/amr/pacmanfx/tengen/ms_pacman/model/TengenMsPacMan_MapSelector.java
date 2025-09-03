/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.NonArcadeMapsSpriteSheet;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.nes.NES_ColorScheme.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_MapSelector implements MapSelector {

    private final Map<MapCategory, List<WorldMap>> mapRepository = new EnumMap<>(MapCategory.class);

    @Override
    public List<WorldMap> builtinMaps() {
        return mapRepository.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public List<WorldMap> customMaps() {
        return List.of();
    }

    @Override
    public void loadAllMaps() {
        if (mapRepository.isEmpty()) {
            mapRepository.put(MapCategory.ARCADE,  MapSelector.loadMapsFromModule(getClass(), MAPS_PATH + "arcade%d.world",     4));
            mapRepository.put(MapCategory.MINI,    MapSelector.loadMapsFromModule(getClass(), MAPS_PATH + "mini%d.world",       6));
            mapRepository.put(MapCategory.BIG,     MapSelector.loadMapsFromModule(getClass(), MAPS_PATH + "big%02d.world",     11));
            mapRepository.put(MapCategory.STRANGE, MapSelector.loadMapsFromModule(getClass(), MAPS_PATH + "strange%02d.world", 15));
        }
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public WorldMap getWorldMap(int levelNumber) {
        throw new UnsupportedOperationException(); //TODO ugly! Reconsider API
    }

    public WorldMap createConfiguredWorldMap(MapCategory mapCategory, int levelNumber) {
        requireNonNull(mapCategory);
        requireValidLevelNumber(levelNumber);
        return switch (mapCategory) {
            case ARCADE -> createConfiguredArcadeMap(levelNumber);
            case MINI   -> createConfiguredMiniMap(levelNumber);
            case BIG    -> createConfiguredBigMap(levelNumber);
            case STRANGE -> {
                WorldMap worldMap = createConfiguredStrangeMap(levelNumber);
                // Hack: Store mazeID in map properties to make renderer happy
                worldMap.setConfigValue("mazeID", NonArcadeMapsSpriteSheet.MazeID.values()[levelNumber - 1]);
                yield worldMap;
            }
        };
    }

    private WorldMap configuration(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = WorldMap.copyOfMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue(PROPERTY_MAP_CATEGORY, category);
        worldMap.setConfigValue(PROPERTY_MAP_NUMBER, number);
        worldMap.setConfigValue(PROPERTY_NES_COLOR_SCHEME, colorScheme);
        worldMap.setConfigValue(PROPERTY_MULTIPLE_FLASH_COLORS, false);
        return worldMap;
    }

    private WorldMap randomConfiguration(MapCategory category, int number) {
        WorldMap worldMap = configuration(category, number, NES_ColorScheme.randomScheme());
        worldMap.setConfigValue(PROPERTY_MULTIPLE_FLASH_COLORS, true);
        return worldMap;
    }

    private WorldMap createConfiguredArcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> configuration(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> configuration(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> configuration(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> configuration(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> configuration(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> configuration(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> configuration(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> configuration(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> configuration(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createConfiguredMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configuration(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configuration(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configuration(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configuration(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configuration(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configuration(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> configuration(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configuration(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configuration(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuration(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuration(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuration(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuration(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuration(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuration(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuration(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuration(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuration(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuration(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuration(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuration(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuration(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuration(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuration(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuration(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuration(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuration(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomConfiguration(MINI, 4);
            case 29 -> randomConfiguration(MINI, 5);
            case 30 -> randomConfiguration(MINI, 2);
            case 31 -> randomConfiguration(MINI, 3);
            case 32 -> configuration(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createConfiguredBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configuration(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configuration(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configuration(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configuration(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configuration(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configuration(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> configuration(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configuration(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configuration(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuration(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuration(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuration(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuration(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuration(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuration(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuration(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuration(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuration(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuration(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuration(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuration(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuration(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuration(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuration(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuration(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuration(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuration(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomConfiguration(BIG,  5);
            case 29 -> randomConfiguration(BIG,  9);
            case 30 -> randomConfiguration(BIG,  2);
            case 31 -> randomConfiguration(BIG, 10);
            case 32 -> configuration(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createConfiguredStrangeMap(int levelNumber) {
        return switch (levelNumber) {
            case  1 -> configuration(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> configuration(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> configuration(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> configuration(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> configuration(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> configuration(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> configuration(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> configuration(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> configuration(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuration(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuration(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuration(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuration(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuration(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuration(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuration(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuration(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuration(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuration(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuration(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuration(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuration(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuration(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuration(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuration(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuration(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuration(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomConfiguration(MINI,     5);
            case 29 -> randomConfiguration(STRANGE,  8);
            case 30 -> randomConfiguration(MINI,     4);
            case 31 -> randomConfiguration(STRANGE, 11);
            case 32 -> configuration(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }
}