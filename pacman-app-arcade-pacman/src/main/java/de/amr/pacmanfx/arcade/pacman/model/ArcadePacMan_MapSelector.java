/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.UIConfig;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;

public class ArcadePacMan_MapSelector implements WorldMapSelector {

    private static final String PROTOTYPE_PATH = "/de/amr/pacmanfx/arcade/pacman/maps/pacman.world";

    private WorldMap prototype;

    @Override
    public void loadMapPrototypes() {
        final URL url = getClass().getResource(PROTOTYPE_PATH);
        if (url == null) {
            final String errorMsg = "Could not access Arcade Pac-Man map '%s'".formatted(PROTOTYPE_PATH);
            Logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        try {
            prototype = WorldMap.create(url);
            Logger.info("Loaded world map '{}'", url);
        } catch (IOException x) {
            Logger.error("Could not open world map");
            throw new RuntimeException(x);
        }
        catch (WorldMapParseException x) {
            Logger.error("Could not parse world map");
            throw new RuntimeException(x);
        }
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
        worldMap.setConfigValue(UIConfig.WorldMapConfigKey.MAP_NUMBER, 1);
        worldMap.setConfigValue(UIConfig.WorldMapConfigKey.COLOR_MAP_INDEX, 0);
        worldMap.setConfigValue(UIConfig.WorldMapConfigKey.COLOR_SCHEME, WorldMapSelector.extractColorScheme(prototype));
        return worldMap;
    }
}