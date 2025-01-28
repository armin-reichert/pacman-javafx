/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.tilemap.WorldMap;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface CustomMapsHandler {
    File customMapDir();
    void updateCustomMaps();
    Map<File, WorldMap> customMapsByFile();
    List<WorldMap> customMapsSortedByFile();
    CustomMapSelectionMode mapSelectionMode();
    void setMapSelectionMode(CustomMapSelectionMode mode);
}
