/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.ResourceRoot;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MapSelector;
import org.tinylog.Logger;

import java.net.URL;

public class ArcadePacMan_MapSelector implements MapSelector {

    private WorldMap map;

    @Override
    public void loadAllMaps(GameModel game) {
        String path = "/de/amr/games/pacman/arcade/maps/pacman.world";
        URL url = ResourceRoot.class.getResource(path);
        if (url == null) {
            Logger.error("Could not load map from path {}", path);
        }
        try {
            map = new WorldMap(url);
            map.setConfigValue("mapNumber", 1);
            map.setConfigValue("colorMapIndex", 0);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public WorldMap selectWorldMap(int levelNumber) {
        return map;
    }
}
