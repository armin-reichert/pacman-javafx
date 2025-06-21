/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public interface MapSelector {
    WorldMap getWorldMap(int levelNumber);
    List<WorldMap> builtinMaps();
    List<WorldMap> customMaps();
    void loadCustomMaps();
    void loadAllMaps();

}