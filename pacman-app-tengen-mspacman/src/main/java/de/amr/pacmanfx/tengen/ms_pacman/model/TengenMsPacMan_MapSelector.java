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

    private WorldMap createConfiguredWorldMap(MapCategory category, int number, NES_ColorScheme colorScheme) {
        WorldMap worldMap = WorldMap.copyMap(mapRepository.get(category).get(number - 1));
        worldMap.setConfigValue("mapCategory", category);
        worldMap.setConfigValue("mapNumber", number);
        worldMap.setConfigValue("nesColorScheme", colorScheme);
        worldMap.setConfigValue("multipleFlashColors", false);
        return worldMap;
    }

    private WorldMap createRandomlyColoredWorldMap(MapCategory category, int number) {
        WorldMap worldMap = createConfiguredWorldMap(category, number, NES_ColorScheme.randomScheme());
        worldMap.setConfigValue("multipleFlashColors", true);
        return worldMap;
    }

    private WorldMap createConfiguredArcadeMap(int levelNumber) {
        return switch (levelNumber) {
            case 1,2         -> createConfiguredWorldMap(ARCADE, 1, _36_15_20_PINK_RED_WHITE);
            case 3,4,5       -> createConfiguredWorldMap(ARCADE, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 6,7,8,9     -> createConfiguredWorldMap(ARCADE, 3, _16_20_15_ORANGE_WHITE_RED);
            case 10,11,12,13 -> createConfiguredWorldMap(ARCADE, 4, _01_38_20_BLUE_YELLOW_WHITE);
            case 14,15,16,17 -> createConfiguredWorldMap(ARCADE, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 18,19,20,21 -> createConfiguredWorldMap(ARCADE, 4, _36_15_20_PINK_RED_WHITE);
            case 22,23,24,25 -> createConfiguredWorldMap(ARCADE, 3, _17_20_20_BROWN_WHITE_WHITE);
            case 26,27,28,29 -> createConfiguredWorldMap(ARCADE, 4, _13_20_28_VIOLET_WHITE_YELLOW);
            case 30,31,32    -> createConfiguredWorldMap(ARCADE, 3, _0F_20_28_BLACK_WHITE_YELLOW);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createConfiguredMiniMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createConfiguredWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 2  -> createConfiguredWorldMap(MINI, 2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createConfiguredWorldMap(MINI, 1, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> createConfiguredWorldMap(MINI, 2, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createConfiguredWorldMap(MINI, 3, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createConfiguredWorldMap(MINI, 1, _36_15_20_PINK_RED_WHITE);
            case 7  -> createConfiguredWorldMap(MINI, 2, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createConfiguredWorldMap(MINI, 3, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createConfiguredWorldMap(MINI, 4, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createConfiguredWorldMap(MINI, 1, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createConfiguredWorldMap(MINI, 2, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createConfiguredWorldMap(MINI, 3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createConfiguredWorldMap(MINI, 4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createConfiguredWorldMap(MINI, 1, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createConfiguredWorldMap(MINI, 2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createConfiguredWorldMap(MINI, 3, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createConfiguredWorldMap(MINI, 4, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createConfiguredWorldMap(MINI, 5, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createConfiguredWorldMap(MINI, 5, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createConfiguredWorldMap(MINI, 4, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createConfiguredWorldMap(MINI, 3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createConfiguredWorldMap(MINI, 2, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createConfiguredWorldMap(MINI, 1, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createConfiguredWorldMap(MINI, 6, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createConfiguredWorldMap(MINI, 1, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createConfiguredWorldMap(MINI, 2, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createConfiguredWorldMap(MINI, 3, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredWorldMap(MINI, 4);
            case 29 -> createRandomlyColoredWorldMap(MINI, 5);
            case 30 -> createRandomlyColoredWorldMap(MINI, 2);
            case 31 -> createRandomlyColoredWorldMap(MINI, 3);
            case 32 -> createConfiguredWorldMap(MINI, 6, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createConfiguredBigMap(int levelNumber) {
        return switch (levelNumber) {
            case 1  -> createConfiguredWorldMap(BIG,  1, _36_15_20_PINK_RED_WHITE);
            case 2  -> createConfiguredWorldMap(BIG,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case 3  -> createConfiguredWorldMap(BIG,  3, _16_20_15_ORANGE_WHITE_RED);
            case 4  -> createConfiguredWorldMap(BIG,  1, _01_38_20_BLUE_YELLOW_WHITE);
            case 5  -> createConfiguredWorldMap(BIG,  2, _35_28_20_PINK_YELLOW_WHITE);
            case 6  -> createConfiguredWorldMap(BIG,  3, _36_15_20_PINK_RED_WHITE);
            case 7  -> createConfiguredWorldMap(BIG,  4, _17_20_20_BROWN_WHITE_WHITE);
            case 8  -> createConfiguredWorldMap(BIG,  5, _13_20_28_VIOLET_WHITE_YELLOW);
            case 9  -> createConfiguredWorldMap(BIG,  6, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createConfiguredWorldMap(BIG,  7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createConfiguredWorldMap(BIG,  5, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createConfiguredWorldMap(BIG,  3, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createConfiguredWorldMap(BIG,  4, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createConfiguredWorldMap(BIG,  8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createConfiguredWorldMap(BIG,  2, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createConfiguredWorldMap(BIG,  1, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createConfiguredWorldMap(BIG,  7, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createConfiguredWorldMap(BIG,  6, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createConfiguredWorldMap(BIG,  7, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createConfiguredWorldMap(BIG,  1, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createConfiguredWorldMap(BIG,  9, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createConfiguredWorldMap(BIG,  3, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createConfiguredWorldMap(BIG,  4, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createConfiguredWorldMap(BIG,  5, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createConfiguredWorldMap(BIG,  8, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createConfiguredWorldMap(BIG, 10, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createConfiguredWorldMap(BIG,  8, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredWorldMap(BIG,  5);
            case 29 -> createRandomlyColoredWorldMap(BIG,  9);
            case 30 -> createRandomlyColoredWorldMap(BIG,  2);
            case 31 -> createRandomlyColoredWorldMap(BIG, 10);
            case 32 -> createConfiguredWorldMap(BIG, 11, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }

    private WorldMap createConfiguredStrangeMap(int levelNumber) {
        return switch (levelNumber) {
            case  1 -> createConfiguredWorldMap(STRANGE,  1, _36_15_20_PINK_RED_WHITE);
            case  2 -> createConfiguredWorldMap(STRANGE,  2, _21_20_28_BLUE_WHITE_YELLOW);
            case  3 -> createConfiguredWorldMap(STRANGE,  3, _16_20_15_ORANGE_WHITE_RED);
            case  4 -> createConfiguredWorldMap(STRANGE,  4, _01_38_20_BLUE_YELLOW_WHITE);
            case  5 -> createConfiguredWorldMap(STRANGE,  5, _35_28_20_PINK_YELLOW_WHITE);
            case  6 -> createConfiguredWorldMap(STRANGE,  6, _36_15_20_PINK_RED_WHITE);
            case  7 -> createConfiguredWorldMap(STRANGE,  7, _17_20_20_BROWN_WHITE_WHITE);
            case  8 -> createConfiguredWorldMap(STRANGE,  8, _13_20_28_VIOLET_WHITE_YELLOW);
            case  9 -> createConfiguredWorldMap(STRANGE,  9, _0F_20_28_BLACK_WHITE_YELLOW);
            case 10 -> createConfiguredWorldMap(BIG,      7, _0F_01_20_BLACK_BLUE_WHITE);
            case 11 -> createConfiguredWorldMap(STRANGE, 10, _14_25_20_VIOLET_ROSE_WHITE);
            case 12 -> createConfiguredWorldMap(STRANGE, 11, _15_20_20_RED_WHITE_WHITE);
            case 13 -> createConfiguredWorldMap(STRANGE,  6, _1B_20_20_GREEN_WHITE_WHITE);
            case 14 -> createConfiguredWorldMap(BIG,      8, _28_20_2A_YELLOW_WHITE_GREEN);
            case 15 -> createConfiguredWorldMap(STRANGE, 12, _1A_20_28_GREEN_WHITE_YELLOW);
            case 16 -> createConfiguredWorldMap(MINI,     5, _18_20_20_KHAKI_WHITE_WHITE);
            case 17 -> createConfiguredWorldMap(BIG,      6, _25_20_20_ROSE_WHITE_WHITE);
            case 18 -> createConfiguredWorldMap(STRANGE, 13, _12_20_28_BLUE_WHITE_YELLOW);
            case 19 -> createConfiguredWorldMap(BIG,      1, _07_20_20_BROWN_WHITE_WHITE);
            case 20 -> createConfiguredWorldMap(BIG,      2, _15_25_20_RED_ROSE_WHITE);
            case 21 -> createConfiguredWorldMap(BIG,      3, _0F_20_1C_BLACK_WHITE_GREEN);
            case 22 -> createConfiguredWorldMap(BIG,      4, _19_20_20_GREEN_WHITE_WHITE);
            case 23 -> createConfiguredWorldMap(BIG,      5, _0C_20_14_GREEN_WHITE_VIOLET);
            case 24 -> createConfiguredWorldMap(STRANGE,  4, _23_20_2B_VIOLET_WHITE_GREEN);
            case 25 -> createConfiguredWorldMap(BIG,     10, _10_20_28_GRAY_WHITE_YELLOW);
            case 26 -> createConfiguredWorldMap(BIG,      9, _03_20_20_BLUE_WHITE_WHITE);
            case 27 -> createConfiguredWorldMap(STRANGE, 14, _04_20_20_VIOLET_WHITE_WHITE);
            case 28 -> createRandomlyColoredWorldMap(MINI,     5);
            case 29 -> createRandomlyColoredWorldMap(STRANGE,  8);
            case 30 -> createRandomlyColoredWorldMap(MINI,     4);
            case 31 -> createRandomlyColoredWorldMap(STRANGE, 11);
            case 32 -> createConfiguredWorldMap(STRANGE, 15, _15_25_20_RED_ROSE_WHITE);
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
    }
}