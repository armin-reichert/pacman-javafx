/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.util.List;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;

public class ArcadeMsPacMan_MapSelector implements WorldMapSelector {

    public static final WorldMapColorScheme[] WORLD_MAP_COLOR_SCHEMES = {
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("47b7ff", "dedeff", "fcb5ff", "ffff00"),
        new WorldMapColorScheme("de9751", "dedeff", "fcb5ff", "ff0000"),
        new WorldMapColorScheme("2121ff", "ffb751", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("ffb7ff", "ffff00", "fcb5ff", "00ffff"),
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff")
    };

    private static final String WORLD_MAP_PATH_PATTERN = "/de/amr/pacmanfx/arcade/ms_pacman/maps/mspacman_%d.world";

    private List<WorldMap> mapPrototypes = List.of();

    @Override
    public List<WorldMap> builtinMapPrototypes() {
        return mapPrototypes;
    }

    @Override
    public List<WorldMap> customMapPrototypes() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public void loadAllMapPrototypes() {
        if (mapPrototypes.isEmpty()) {
            mapPrototypes = WorldMapSelector.loadMapsFromModule(getClass(), WORLD_MAP_PATH_PATTERN, 4);
        }
    }

    /**
     * <p>In Ms. Pac-Man, there are 4 maps (mapNumber=1..4) and 6 color schemes (colorIndex=0..5).
     * </p>
     * <ul>
     * <li>Levels 1-2: (mapNumber=1, colorIndex=0): pink wall fill, white dots
     * <li>Levels 3-5: (mapNumber=2, colorIndex=1)): light blue wall fill, yellow dots
     * <li>Levels 6-9: (mapNumber=3, colorIndex=2): orange wall fill, red dots
     * <li>Levels 10-13: (mapNumber=4, colorIndex=3): blue wall fill, white dots
     * </ul>
     * For levels 14 and later, alternates every 4th level between:
     * <ul>
     * <li>(mapNumber=3, colorIndex=4): pink wall fill, cyan dots
     * <li>(mapNumber=4, colorIndex=5): orange wall fill, white dots
     * </ul>
     * <p>
     *
     * @param levelNumber level number (starts at 1)
     * @param args additional arguments
     */
    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) {
        requireValidLevelNumber(levelNumber);
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        final WorldMap prototype = mapPrototypes.get(mapNumber - 1);
        return configuredWorldMap(prototype, levelNumber, mapNumber);
    }

    // Color scheme index
    // 1->0, 2->1, 3->2, 4->3   level 1..13;
    // 3->4; 4->5               level 14+
    private WorldMap configuredWorldMap(WorldMap prototype, int levelNumber, int mapNumber) {
        final WorldMap worldMap = new WorldMap(prototype);
        final int colorMapIndex = levelNumber <= 13 ? mapNumber - 1 : mapNumber + 1;
        worldMap.setConfigValue(GameUI_Config.ConfigKey.MAP_NUMBER, mapNumber);
        worldMap.setConfigValue(GameUI_Config.ConfigKey.COLOR_MAP_INDEX, colorMapIndex);
        return worldMap;
    }
}