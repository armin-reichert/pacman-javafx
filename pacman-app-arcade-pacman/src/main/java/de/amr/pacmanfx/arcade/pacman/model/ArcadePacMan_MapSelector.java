/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import org.tinylog.Logger;

import java.net.URL;

public class ArcadePacMan_MapSelector implements WorldMapSelector {

    private static final String PROTOTYPE_PATH = "/de/amr/pacmanfx/arcade/pacman/maps/pacman.world";

    private WorldMap prototype;

    @Override
    public void loadMapPrototypes() {
        final URL url = getClass().getResource(PROTOTYPE_PATH);
        if (url == null) {
            throw new IllegalStateException("Could not access Arcade Pac-Man map '%s'".formatted(PROTOTYPE_PATH));
        }
        WorldMap.fromURL(url).ifPresentOrElse(worldMap -> prototype = worldMap,
            () -> Logger.error("Could not load map prototype from URL {}", url)
        );
    }

    /**
     * @param levelNumber (ignored) level number (starting with 1)
     * @param args (ignored) additional arguments
     * @return the single map used in Arcade Pac-Man
     */
    @Override
    public WorldMap supplyWorldMap(int levelNumber, Object... args) {
        if (prototype == null) {
            loadMapPrototypes();
        }
        final WorldMap worldMap = new WorldMap(prototype);
        worldMap.setConfigValue(WorldMapConfigKey.MAP_NUMBER, 1);
        worldMap.setConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX, 0);
        worldMap.setConfigValue(WorldMapConfigKey.COLOR_SCHEME, WorldMapSelector.extractColorScheme(prototype));
        return worldMap;
    }
}