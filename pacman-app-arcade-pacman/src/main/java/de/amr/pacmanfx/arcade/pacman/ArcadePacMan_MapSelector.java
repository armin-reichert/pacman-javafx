/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.MapSelector;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.*;

public class ArcadePacMan_MapSelector implements MapSelector {

    private static final String MAP_PATH = "/de/amr/pacmanfx/arcade/pacman/maps/pacman.world";

    private List<WorldMap> worldMaps;

    @Override
    public List<WorldMap> builtinMaps() { return worldMaps; }

    @Override
    public List<WorldMap> customMaps() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public void loadAllMaps() {
        if (worldMaps == null) {
            URL mapURL = getClass().getResource(MAP_PATH);
            if (mapURL == null) {
                Logger.error("Could not locate Pac-Man Arcade map, path='{}'", MAP_PATH);
                throw new IllegalStateException();
            }
            try {
                var worldMap = WorldMap.loadFromURL(mapURL);
                worldMap.setConfigValue(PROPERTY_MAP_NUMBER, 1);
                worldMap.setConfigValue(PROPERTY_COLOR_MAP_INDEX, 0);
                worldMap.setConfigValue(PROPERTY_COLOR_MAP, MapSelector.extractColorMap(worldMap));
                Logger.info("Pac-Man Arcade map loaded, URL='{}'", worldMap.url());
                worldMaps = List.of(worldMap);
            } catch (IOException x) {
                Logger.error("Could not load Pac-Man Arcade map, path={}", MAP_PATH);
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public WorldMap getWorldMap(int levelNumber) {
        return worldMaps.getFirst();
    }
}