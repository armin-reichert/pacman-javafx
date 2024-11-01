/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.ms_pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapConfig;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapConfigurationManager {

    private static final String MAP_PATTERN = "/de/amr/games/pacman/maps/mspacman/mspacman_%d.world";

    private static final List<Map<String, String>> COLOR_SCHEMES = List.of(
        Map.of("fill", "FFB7AE", "stroke", "FF0000", "door", "FCB5FF", "pellet", "DEDEFF"),
        Map.of("fill", "47B7FF", "stroke", "DEDEFF", "door", "FCB5FF", "pellet", "FFFF00"),
        Map.of("fill", "DE9751", "stroke", "DEDEFF", "door", "FCB5FF", "pellet", "FF0000"),
        Map.of("fill", "2121FF", "stroke", "FFB751", "door", "FCB5FF", "pellet", "DEDEFF"),
        Map.of("fill", "FFB7FF", "stroke", "FFFF00", "door", "FCB5FF", "pellet", "00FFFF"),
        Map.of("fill", "FFB7AE", "stroke", "FF0000", "door", "FCB5FF", "pellet", "DEDEFF")
    );

    private final List<WorldMap> maps = new ArrayList<>();

    public static int colorSchemeIndex(Map<String, String> colorScheme) {
        return COLOR_SCHEMES.indexOf(colorScheme);
    }

    public MapConfigurationManager() {
        for (int number = 1; number <= 4; ++number) {
            maps.add(new WorldMap(getClass().getResource(MAP_PATTERN.formatted(number))));
        }
        Logger.info("{} maps loaded ({})", maps.size(), GameVariant.MS_PACMAN);
    }

    /**
     * <p>In Ms. Pac-Man, there are 4 maps and 6 color schemes.
     * </p>
     * <ul>
     * <li>Levels 1-2: (1, 1): pink wall fill, white dots
     * <li>Levels 3-5: (2, 2)): light blue wall fill, yellow dots
     * <li>Levels 6-9: (3, 3): orange wall fill, red dots
     * <li>Levels 10-13: (4, 4): blue wall fill, white dots
     * </ul>
     * For level 14 and later, (map, color_scheme) alternates every 4th level between (3, 5) and (4, 6):
     * <ul>
     * <li>(3, 5): pink wall fill, cyan dots
     * <li>(4, 6): orange wall fill, white dots
     * </ul>
     * <p>
     */
    MapConfig getMapConfig(int levelNumber) {
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        final int schemeNumber = levelNumber < 14 ? mapNumber : mapNumber + 2;
        return new MapConfig(mapNumber, new WorldMap(maps.get(mapNumber - 1)), COLOR_SCHEMES.get(schemeNumber - 1));
    }
}