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

public interface MapSelector {

    /**
     * @param mapPattern path (pattern) to access the map files inside resources folder,
     *                   counting from 1, e.g. <code>"maps/masonic_%d.world"</code>
     * @param mapCount number of maps to be loaded
     */
    static List<WorldMap> loadMapsFromModule(Class<?> loaderClass, String mapPattern, int mapCount) {
        var maps = new ArrayList<WorldMap>();
        for (int mapNumber = 1; mapNumber <= mapCount; ++mapNumber) {
            URL url = loaderClass.getResource(mapPattern.formatted(mapNumber));
            if (url == null) {
                Logger.error("Map not found, pattern='{}', number={}", mapPattern, mapNumber);
                throw new IllegalStateException();
            }
            try {
                WorldMap worldMap = WorldMap.fromURL(url);
                maps.add(worldMap);
                Logger.info("Map loaded, URL='{}'", worldMap.url());
            } catch (IOException x) {
                Logger.error("Could not load map, URL='{}'", url);
                throw new IllegalStateException(x);
            }
        }
        return maps;
    }

    WorldMap findWorldMap(int levelNumber);
    List<WorldMap> builtinMaps();
    List<WorldMap> customMaps();
    void loadCustomMaps();
    void loadAllMaps();
}