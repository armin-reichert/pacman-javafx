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

    private static final String PROTOTYPE_PATH = "/de/amr/pacmanfx/arcade/pacman/maps/pacman.world";

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
    public void loadAllMapPrototypes() throws IOException {
        final URL url = getClass().getResource(PROTOTYPE_PATH);
        if (url == null) {
            final String errorMsg = "Could not access Arcade Pac-Man map '%s'".formatted(PROTOTYPE_PATH);
            Logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        prototype = WorldMap.loadFromURL(url);
        Logger.info("Loaded world map '{}'", url);
    }

    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) throws IOException {
        if (prototype == null) {
            loadAllMapPrototypes();
        }
        final WorldMap worldMap = new WorldMap(prototype);
        worldMap.setConfigValue(GameUI_Config.ConfigKey.MAP_NUMBER, 1);
        worldMap.setConfigValue(GameUI_Config.ConfigKey.COLOR_MAP_INDEX, 0);
        worldMap.setConfigValue(GameUI_Config.ConfigKey.COLOR_SCHEME, WorldMapSelector.extractColorScheme(prototype));
        return worldMap;
    }
}