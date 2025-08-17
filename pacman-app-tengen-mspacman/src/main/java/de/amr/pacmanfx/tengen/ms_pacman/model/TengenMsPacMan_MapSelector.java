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
import static de.amr.pacmanfx.lib.tilemap.WorldMap.loadMapsFromModule;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.MAPS_PATH;
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
            mapRepository.put(MapCategory.ARCADE,  loadMapsFromModule(getClass(), MAPS_PATH + "arcade%d.world",     4));
            mapRepository.put(MapCategory.MINI,    loadMapsFromModule(getClass(), MAPS_PATH + "mini%d.world",       6));
            mapRepository.put(MapCategory.BIG,     loadMapsFromModule(getClass(), MAPS_PATH + "big%02d.world",     11));
            mapRepository.put(MapCategory.STRANGE, loadMapsFromModule(getClass(), MAPS_PATH + "strange%02d.world", 15));
        }
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public WorldMap getWorldMap(int levelNumber) {
        throw new UnsupportedOperationException(); //TODO ugly! Reconsider API
    }

    public WorldMap createWorldMapForLevel(MapCategory mapCategory, int levelNumber) {
        requireNonNull(mapCategory);
        requireValidLevelNumber(levelNumber);
        return switch (mapCategory) {
            case ARCADE -> createArcadeMap(levelNumber);
            case MINI   -> createMiniMap(levelNumber);
            case BIG    -> createBigMap(levelNumber);
            case STRANGE -> {
                WorldMap worldMap = createStrangeMap(levelNumber);
                // Hack: Store mazeID in map properties to make renderer happy
                worldMap.setConfigValue("mazeID", NonArcadeMapsSpriteSheet.MazeID.values()[levelNumber - 1]);
                yield worldMap;
            }
        };
    }

    private WorldMap createArcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> configuredWorldMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> configuredWorldMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> configuredWorldMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> configuredWorldMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> configuredWorldMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> configuredWorldMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> configuredWorldMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> configuredWorldMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> configuredWorldMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configuredWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configuredWorldMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configuredWorldMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configuredWorldMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configuredWorldMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configuredWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> configuredWorldMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configuredWorldMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configuredWorldMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuredWorldMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuredWorldMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuredWorldMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuredWorldMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuredWorldMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuredWorldMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuredWorldMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuredWorldMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuredWorldMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuredWorldMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuredWorldMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuredWorldMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuredWorldMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuredWorldMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuredWorldMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuredWorldMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuredWorldMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuredWorldMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyColoredMap(MINI, 4);
            case 29 -> randomlyColoredMap(MINI, 5);
            case 30 -> randomlyColoredMap(MINI, 2);
            case 31 -> randomlyColoredMap(MINI, 3);
            case 32 -> configuredWorldMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> configuredWorldMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> configuredWorldMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> configuredWorldMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> configuredWorldMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> configuredWorldMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> configuredWorldMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> configuredWorldMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> configuredWorldMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> configuredWorldMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuredWorldMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuredWorldMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuredWorldMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuredWorldMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuredWorldMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuredWorldMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuredWorldMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuredWorldMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuredWorldMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuredWorldMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuredWorldMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuredWorldMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuredWorldMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuredWorldMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuredWorldMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuredWorldMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuredWorldMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuredWorldMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyColoredMap(BIG,  5);
            case 29 -> randomlyColoredMap(BIG,  9);
            case 30 -> randomlyColoredMap(BIG,  2);
            case 31 -> randomlyColoredMap(BIG, 10);
            case 32 -> configuredWorldMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createStrangeMap(int levelNumber) {
        return switch (levelNumber) {
            case  1 -> configuredWorldMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> configuredWorldMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> configuredWorldMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> configuredWorldMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> configuredWorldMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> configuredWorldMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> configuredWorldMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> configuredWorldMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> configuredWorldMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> configuredWorldMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> configuredWorldMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> configuredWorldMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> configuredWorldMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> configuredWorldMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> configuredWorldMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> configuredWorldMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> configuredWorldMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> configuredWorldMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> configuredWorldMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> configuredWorldMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> configuredWorldMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> configuredWorldMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> configuredWorldMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> configuredWorldMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> configuredWorldMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> configuredWorldMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> configuredWorldMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> randomlyColoredMap(MINI,     5);
            case 29 -> randomlyColoredMap(STRANGE,  8);
            case 30 -> randomlyColoredMap(MINI,     4);
            case 31 -> randomlyColoredMap(STRANGE, 11);
            case 32 -> configuredWorldMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap configuredWorldMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = WorldMap.copyMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("multipleFlashColors", false);
        return worldMap;
    }

    private WorldMap randomlyColoredMap(MapCategory category, int number) {
        WorldMap worldMap = configuredWorldMap(category, number, NES_ColorScheme.randomScheme());
        worldMap.setConfigValue("multipleFlashColors", true);
        return worldMap;
    }
}