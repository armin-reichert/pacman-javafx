/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.*;

public class ArcadePacMan_MapSelector implements WorldMapSelector {

    private static final String MAP_PROTOTYPE_PATH = "/de/amr/pacmanfx/arcade/pacman/maps/pacman.world";

    private WorldMap worldMapPrototype;

    @Override
    public List<WorldMap> builtinMapPrototypes() { return List.of(worldMapPrototype); }

    @Override
    public List<WorldMap> customMapPrototypes() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public void loadAllMapPrototypes() {
        if (worldMapPrototype == null) {
            URL url = getClass().getResource(MAP_PROTOTYPE_PATH);
            if (url == null) {
                Logger.error("Could not locate Pac-Man Arcade map, path='{}'", MAP_PROTOTYPE_PATH);
                throw new IllegalStateException();
            }
            try {
                worldMapPrototype = WorldMap.loadFromURL(url);
                worldMapPrototype.setConfigValue(CONFIG_KEY_MAP_NUMBER, 1);
                worldMapPrototype.setConfigValue(CONFIG_KEY_COLOR_MAP_INDEX, 0);
                worldMapPrototype.setConfigValue(CONFIG_KEY_COLOR_SCHEME, WorldMapSelector.extractColorScheme(worldMapPrototype));
                Logger.info("Pac-Man Arcade map loaded, URL='{}'", worldMapPrototype.url());
            } catch (IOException x) {
                Logger.error("Could not load Pac-Man Arcade map, URL={}", url);
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber, Object... args) {
        return new WorldMap(worldMapPrototype);
    }
}