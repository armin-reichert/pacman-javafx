/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.MapSelector;

import java.util.List;

import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig.PROPERTY_COLOR_MAP;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig.PROPERTY_COLOR_MAP_INDEX;

public class ArcadeMsPacMan_MapSelector implements MapSelector {

    private List<WorldMap> maps = List.of();

    @Override
    public List<WorldMap> builtinMaps() {
        return maps;
    }

    @Override
    public List<WorldMap> customMaps() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public void loadAllMaps() {
        if (maps.isEmpty()) {
            maps = MapSelector.loadMapsFromModule(getClass(), "maps/mspacman_%d.world", 4);
        }
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
    @Override
    public WorldMap getWorldMap(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        var worldMap = WorldMap.copyOfMap(maps.get(mapNumber - 1));
        worldMap.setConfigValue("mapNumber", mapNumber);

        // 1->0, 2->1, 3->2, 4->3 if level <= 13; 3->4; 4->5 if level >= 14
        int index = levelNumber < 14 ? mapNumber - 1 : mapNumber + 1;
        worldMap.setConfigValue(PROPERTY_COLOR_MAP_INDEX, index);
        worldMap.setConfigValue(PROPERTY_COLOR_MAP,
            ArcadeMsPacMan_GameModel.WORLD_MAP_COLOR_SCHEMES.get(index).toColorMap());

        return worldMap;
    }
}