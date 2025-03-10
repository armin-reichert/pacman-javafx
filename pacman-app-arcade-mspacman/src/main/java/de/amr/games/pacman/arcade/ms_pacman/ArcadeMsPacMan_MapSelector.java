/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapSelector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ArcadeMsPacMan_MapSelector extends MapSelector {

    private ObservableList<WorldMap> maps = FXCollections.emptyObservableList();

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
    public void loadAllMaps(GameModel game) {
        if (maps.isEmpty()) {
            maps = FXCollections.observableList(loadMapsFromModule("maps/mspacman_%d.world", 4));
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
    public WorldMap selectWorldMap(int levelNumber) {
        final int mapNumber = switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 3 : 4;
        };
        int colorMapIndex = levelNumber < 14 ? mapNumber - 1 : mapNumber + 2 - 1;
        WorldMap worldMap = new WorldMap(maps.get(mapNumber - 1));
        worldMap.setConfigValue("mapNumber", mapNumber);
        worldMap.setConfigValue("colorMapIndex", colorMapIndex);
        return worldMap;
    }
}