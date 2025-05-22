/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class MapSelector {

    private MapSelectionMode mapSelectionMode = MapSelectionMode.NO_CUSTOM_MAPS;

    /**
     * @param mapPattern path (pattern) to access the map files inside resources folder,
     *                   counting from 1, e.g. <code>"maps/masonic_%d.world"</code>
     * @param mapCount number of maps to be loaded
     */
    public List<WorldMap> loadMapsFromModule(String mapPattern, int mapCount) {
        var maps = new ArrayList<WorldMap>();
        for (int mapNumber = 1; mapNumber <= mapCount; ++mapNumber) {
            URL url = getClass().getResource(mapPattern.formatted(mapNumber));
            if (url == null) {
                Logger.error("Map not found, pattern='{}', number={}", mapPattern, mapNumber);
                throw new IllegalStateException();
            }
            try {
                WorldMap worldMap = new WorldMap(url);
                maps.add(worldMap);
                Logger.info("Map loaded, URL='{}'", worldMap.url());
            } catch (IOException x) {
                Logger.error("Could not load map, URL='{}'", url);
                throw new IllegalStateException(x);
            }
        }
        return maps;
    }

    public MapSelectionMode mapSelectionMode() { return mapSelectionMode; }

    public void setMapSelectionMode(MapSelectionMode mode) { mapSelectionMode = requireNonNull(mode); }

    public abstract WorldMap findWorldMap(int levelNumber);

    public abstract List<WorldMap> builtinMaps();

    public List<WorldMap> customMaps() {
        return List.of();
    }

    public void loadCustomMaps() {}

    public abstract void loadAllMaps();
}