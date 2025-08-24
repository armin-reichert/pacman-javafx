/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;

import java.util.List;
import java.util.Map;

public interface MapSelector {
    WorldMap getWorldMap(int levelNumber);
    List<WorldMap> builtinMaps();
    List<WorldMap> customMaps();
    void loadCustomMaps();
    void loadAllMaps();

    static Map<String, String> extractColorMap(WorldMap worldMap) {
        return Map.of(
            "fill",   worldMap.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_WALL_FILL,   "000000"),
            "stroke", worldMap.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_WALL_STROKE, "0000ff"),
            "door",   worldMap.properties(LayerID.TERRAIN).getOrDefault(WorldMapProperty.COLOR_DOOR,        "00ffff"),
            "pellet", worldMap.properties(LayerID.FOOD).getOrDefault(WorldMapProperty.COLOR_FOOD,           "ffffff")
        );
    }
}