/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class MapSelector {

    private MapSelectionMode mapSelectionMode;

    public MapSelector() {
        this.mapSelectionMode = MapSelectionMode.NO_CUSTOM_MAPS;
    }

    /**
     * @param mapPattern path (pattern) to access the map files inside resources folder,
     *                   counting from 1, e.g. <code>"maps/masonic_%d.world"</code>
     * @param mapCount number of maps to be loaded
     */
    public List<WorldMap> loadMapsFromModule(String mapPattern, int mapCount) {
        var maps = new ArrayList<WorldMap>();
        for (int mapNumber = 1; mapNumber <= mapCount; ++mapNumber) {
            URL url = getClass().getResource(mapPattern.formatted(mapNumber));
            if (url != null) {
                try {
                    WorldMap worldMap = new WorldMap(url);
                    maps.add(worldMap);
                } catch (IOException x) {
                    Logger.error(x);
                    Logger.error("Could not create world map, url={}", url);
                }
            } else {
                Logger.error("Could not load world map, pattern={}, number={}", mapPattern, mapNumber);
            }
        }
        Logger.info("{} maps loaded ({})", maps.size(), getClass().getSimpleName());
        return maps;
    }

    public final MapSelectionMode mapSelectionMode() { return mapSelectionMode; }

    public void setMapSelectionMode(MapSelectionMode mode) { mapSelectionMode = mode; }

    public abstract WorldMap selectWorldMap(int levelNumber);

    public abstract List<WorldMap> builtinMaps();

    public List<WorldMap> customMaps() {
        return List.of();
    }

    public void loadCustomMaps() {}

    public abstract void loadAllMaps(GameModel game);
}