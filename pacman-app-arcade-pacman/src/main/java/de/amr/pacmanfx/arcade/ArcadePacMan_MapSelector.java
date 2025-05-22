/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.MapSelector;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ArcadePacMan_MapSelector implements MapSelector {

    private static final String MAP_PATH = "/de/amr/pacmanfx/arcade/maps/pacman.world";

    private WorldMap theMap;

    @Override
    public List<WorldMap> builtinMaps() {
        return List.of(theMap);
    }

    @Override
    public List<WorldMap> customMaps() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {
    }

    @Override
    public void loadAllMaps() {
        if (theMap == null) {
            URL mapURL = getClass().getResource(MAP_PATH);
            if (mapURL == null) {
                Logger.error("Could not locate Pac-Man Arcade map, path='{}'", MAP_PATH);
                throw new IllegalStateException();
            }
            try {
                theMap = new WorldMap(mapURL);
                theMap.setConfigValue("mapNumber", 1);
                theMap.setConfigValue("colorMapIndex", 0);
                Logger.info("Pac-Man Arcade map loaded, URL='{}'", theMap.url());
            } catch (IOException x) {
                Logger.error("Could not load Pac-Man Arcade map, path={}", MAP_PATH);
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public WorldMap findWorldMap(int levelNumber) {
        return theMap;
    }
}