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
     * @param levelNumber level number (starting with 1)
     * @param args additional arguments
     * @return New copy of the world map prototype for specified level.
     */
    WorldMap supplyWorldMap(int levelNumber, Object... args) throws IOException;

    /**
     * @return list of the built-in maps. Users should create a copy because the prototypes are modifiable!
     */
    default List<WorldMap> customMapPrototypes() {
        return List.of();
    }

    /**
     * Loads all user-defined maps (XXL games only).
     * @throws IOException if map loading fails
     */
    default void loadCustomMaps() throws IOException {}

    /**
     * Loads all map prototypes, built-in and user-defined maps.
     * @throws IOException if map loading fails
     */
    void loadAllMapPrototypes() throws IOException;

    /**
     * @param mapPattern path (pattern) to access the map files inside resources folder,
     *                   counting from 1, e.g. <code>"maps/masonic_%d.world"</code>
     * @param mapCount number of maps to be loaded
     * @throws IllegalArgumentException if a map cannot be accessed via a URL created from the pattern
     * @throws IOException if map loading fails
     */
    static List<WorldMap> loadMaps(Class<?> loaderClass, String mapPattern, int mapCount) throws IOException {
        final var maps = new ArrayList<WorldMap>();
        for (int n = 1; n <= mapCount; ++n) {
            final String name = mapPattern.formatted(n);
            final URL url = loaderClass.getResource(name);
            if (url == null) {
                Logger.error("Map not found for resource name='{}'", name);
                throw new IllegalArgumentException("Illegal map pattern: " + name);
            }
            final WorldMap worldMap = WorldMap.loadFromURL(url);
            maps.add(worldMap);
            Logger.info("Map loaded, URL='{}'", url);
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