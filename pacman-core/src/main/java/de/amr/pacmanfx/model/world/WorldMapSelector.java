/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.world;

import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public interface WorldMapSelector {

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

    void loadCustomMaps();

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

    static WorldMapColorScheme extractColorScheme(WorldMap worldMap) {
        return new WorldMapColorScheme(
            worldMap.terrainLayer().propertyMap().getOrDefault(WorldMapPropertyName.COLOR_WALL_FILL,   "000000"),
            worldMap.terrainLayer().propertyMap().getOrDefault(WorldMapPropertyName.COLOR_WALL_STROKE, "0000ff"),
            worldMap.terrainLayer().propertyMap().getOrDefault(WorldMapPropertyName.COLOR_DOOR,        "00ffff"),
            worldMap.foodLayer()   .propertyMap().getOrDefault(WorldMapPropertyName.COLOR_FOOD,        "ffffff")
        );
    }
}