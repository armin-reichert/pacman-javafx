/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ArcadePacMan_MapSelector implements WorldMapSelector {

    private static final String MAP_PROTOTYPE_PATH = "/de/amr/pacmanfx/arcade/pacman/maps/pacman.world";

    private WorldMap prototype;

    @Override
    public List<WorldMap> builtinMapPrototypes() { return List.of(prototype); }

    @Override
    public List<WorldMap> customMapPrototypes() {
        return List.of();
    }

    @Override
    public void loadCustomMaps() {}

    @Override
    public void loadAllMapPrototypes() {
        final URL url = getClass().getResource(MAP_PROTOTYPE_PATH);
        if (url == null) {
            Logger.error("Could not locate Pac-Man Arcade map, path='{}'", MAP_PROTOTYPE_PATH);
            throw new IllegalStateException();
        }
        try {
            prototype = WorldMap.loadFromURL(url);
            prototype.setConfigValue(GameUI_Config.ConfigKey.MAP_NUMBER, 1);
            prototype.setConfigValue(GameUI_Config.ConfigKey.COLOR_MAP_INDEX, 0);
            prototype.setConfigValue(GameUI_Config.ConfigKey.COLOR_SCHEME, WorldMapSelector.extractColorScheme(prototype));
            Logger.info("Pac-Man Arcade map loaded, URL='{}'", prototype.url());
        } catch (IOException x) {
            Logger.error("Could not load Pac-Man Arcade map, URL={}", url);
            throw new IllegalStateException(x);
        }
    }

    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) {
        if (prototype == null) {
            loadAllMapPrototypes();
        }
        return new WorldMap(prototype);
    }
}