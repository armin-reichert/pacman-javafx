/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;

import java.util.List;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig.CONFIG_KEY_COLOR_MAP;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig.CONFIG_KEY_COLOR_MAP_INDEX;
import static de.amr.pacmanfx.ui.api.GameUI_Config.CONFIG_KEY_MAP_NUMBER;

public class ArcadeMsPacMan_MapSelector implements MapSelector {

    public static final List<WorldMapColorScheme> WORLD_MAP_COLOR_SCHEMES = List.of(
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("47B7FF", "DEDEFF", "FCB5FF", "FFFF00"),
        new WorldMapColorScheme("DE9751", "DEDEFF", "FCB5FF", "FF0000"),
        new WorldMapColorScheme("2121FF", "FFB751", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("FFB7FF", "FFFF00", "FCB5FF", "00FFFF"),
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF")
    );

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
    public void loadCustomMapPrototypes() {}

    @Override
    public void loadAllMapPrototypes() {
        if (mapPrototypes.isEmpty()) {
            mapPrototypes = MapSelector.loadMapsFromModule(getClass(), "maps/mspacman_%d.world", 4);
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
     */
    @Override
    public WorldMap provideWorldMap(int levelNumber, Object... args) {
        requireValidLevelNumber(levelNumber);

        // Map number
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        WorldMap prototype = mapPrototypes.get(mapNumber - 1);
        WorldMap worldMap = new WorldMap(prototype);
        worldMap.setConfigValue(CONFIG_KEY_MAP_NUMBER, mapNumber);
        // Color scheme index
        // 1->0, 2->1, 3->2, 4->3   level 1..13;
        // 3->4; 4->5               level 14+
        int colorIndex = levelNumber <= 13 ? mapNumber - 1 : mapNumber + 1;
        worldMap.setConfigValue(CONFIG_KEY_COLOR_MAP_INDEX, colorIndex);
        worldMap.setConfigValue(CONFIG_KEY_COLOR_MAP, WORLD_MAP_COLOR_SCHEMES.get(colorIndex).toColorMap());

        return worldMap;
    }
}