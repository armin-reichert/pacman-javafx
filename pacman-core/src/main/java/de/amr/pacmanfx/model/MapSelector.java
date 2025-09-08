/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface MapSelector {
    WorldMap getWorldMap(int levelNumber);
    List<WorldMap> builtinMaps();
    List<WorldMap> customMaps();
    void loadCustomMaps();
    void loadAllMaps();

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
                throw new IllegalArgumentException();
            }
            try {
                WorldMap worldMap = WorldMap.mapFromURL(url);
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
            "fill",   worldMap.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_WALL_FILL,   "000000"),
            "stroke", worldMap.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_WALL_STROKE, "0000ff"),
            "door",   worldMap.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_DOOR,        "00ffff"),
            "pellet", worldMap.properties(LayerID.FOOD).getOrDefault(WorldMapProperty.COLOR_FOOD,           "ffffff")
        );
    }
}