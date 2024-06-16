/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui2d.GameContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Armin Reichert
 */
public class InfoCustomMaps extends InfoBox {

    @Override
    public void init(GameContext context) {
        this.context = context;

        var pattern = Pattern.compile(".*/(.*\\.world)$");
        for (var map : GameController.it().getCustomMaps()) {
            if (map.url() != null) {
                Matcher m = pattern.matcher(map.url().toExternalForm());
                if (m.matches()) {
                    String mapName = m.group(1);
                    String mapSize = "(%dx%d)".formatted(map.numRows(), map.numCols());
                    infoText(mapName, mapSize);
                }
            }
        }
    }
}
