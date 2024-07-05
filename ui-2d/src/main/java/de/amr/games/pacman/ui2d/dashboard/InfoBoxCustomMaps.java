/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.ui2d.GameContext;

import java.io.File;

/**
 * @author Armin Reichert
 */
public class InfoBoxCustomMaps extends InfoBox {

    @Override
    public void init(GameContext context) {
        this.context = context;
        var dict = GameController.it().customMapsDict();
        if (dict.isEmpty()) {
            infoText("No custom maps found.", "");
            return;
        }
        for (File mapFile : dict.keySet().stream().sorted().toList()) {
            WorldMap map = dict.get(mapFile);
            String mapSize = "(%dx%d)".formatted(map.numRows(), map.numCols());
            infoText(mapFile.getName(), mapSize);
        }
    }
}
