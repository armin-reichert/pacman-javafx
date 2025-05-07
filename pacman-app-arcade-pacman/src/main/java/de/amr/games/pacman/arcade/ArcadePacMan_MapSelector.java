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

    private WorldMap theMap;

    @Override
    public List<WorldMap> builtinMaps() {
        return List.of(theMap);
    }

    @Override
    public void loadAllMaps(GameModel game) {
        if (theMap == null) {
            try {
                theMap = new WorldMap(getClass().getResource(MAP_PATH));
                theMap.setConfigValue("mapNumber", 1);
                theMap.setConfigValue("colorMapIndex", 0);
                Logger.info("Pac-Man Arcade map loaded, URL={}", theMap.url());
            } catch (Exception x) {
                Logger.error("Could not load map from path {}", MAP_PATH);
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber) {
        return theMap;
    }
}