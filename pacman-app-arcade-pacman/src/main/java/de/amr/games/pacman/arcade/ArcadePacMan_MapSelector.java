/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapSelector;
import org.tinylog.Logger;

import java.util.List;

public class ArcadePacMan_MapSelector extends MapSelector {

    private static final String MAP_PATH = "/de/amr/games/pacman/arcade/maps/pacman.world";

    private List<WorldMap> maps = List.of();

    @Override
    public List<WorldMap> builtinMaps() {
        return maps;
    }

    @Override
    public void loadAllMaps(GameModel game) {
        if (maps.isEmpty()) {
            try {
                WorldMap map = new WorldMap(getClass().getResource(MAP_PATH));
                map.setConfigValue("mapNumber", 1);
                map.setConfigValue("colorMapIndex", 0);
                maps = List.of(map);
            } catch (Exception x) {
                Logger.error("Could not load map from path {}", MAP_PATH);
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber) {
        return maps.getFirst();
    }
}