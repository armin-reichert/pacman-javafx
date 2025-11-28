/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface MapSelector {

    /**
     * @param levelNumber level number (1..)
     * @param args additional arguments
     * @return Fresh copy of the world map prototype for specified level.
     */
    WorldMap selectWorldMap(int levelNumber, Object... args);

    /**
     * @return list of the built-in maps. Users should create a copy because the prototypes are modifiable!
     */
    List<WorldMap> builtinMapPrototypes();

    /**
     * @return list of the built-in maps. Users should create a copy because the prototypes are modifiable!
     */
    List<WorldMap> customMapPrototypes();

    void loadCustomMapPrototypes();

    void loadAllMapPrototypes();

    /**
     * @param mapPattern path (pattern) to access the map files inside resources folder,
     *                   counting from 1, e.g. <code>"maps/masonic_%d.world"</code>
     * @param mapCount number of maps to be loaded
     */
    static List<WorldMap> loadMapsFromModule(Class<?> loaderClass, String mapPattern, int mapCount) {
        var maps = new ArrayList<WorldMap>();
        for (int n = 1; n <= mapCount; ++n) {
            String name = mapPattern.formatted(n);
            URL url = loaderClass.getResource(name);
            if (url == null) {
                Logger.error("Map not found for resource name='{}'", name);
                throw new IllegalArgumentException("Illegal map pattern: " + name);
            }
            try {
                WorldMap worldMap = WorldMap.loadFromURL(url);
                maps.add(worldMap);
                Logger.info("Map loaded, URL='{}'", url);
            } catch (IOException x) {
                Logger.error("Could not load map, URL='{}'", url);
                throw new IllegalArgumentException(x);
            }
        }
        return maps;
    }

    static Map<String, String> extractColorMap(WorldMap worldMap) {
        return Map.of(
            "fill",   worldMap.terrainLayer().propertyMap().getOrDefault(DefaultWorldMapPropertyName.COLOR_WALL_FILL,   "000000"),
            "stroke", worldMap.terrainLayer().propertyMap().getOrDefault(DefaultWorldMapPropertyName.COLOR_WALL_STROKE, "0000ff"),
            "door",   worldMap.terrainLayer().propertyMap().getOrDefault(DefaultWorldMapPropertyName.COLOR_DOOR,        "00ffff"),
            "pellet", worldMap.foodLayer()   .propertyMap().getOrDefault(DefaultWorldMapPropertyName.COLOR_FOOD,        "ffffff")
        );
    }
}