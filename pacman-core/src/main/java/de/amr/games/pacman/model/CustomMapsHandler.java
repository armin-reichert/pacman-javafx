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
    MapSelectionMode mapSelectionMode();
    void setMapSelectionMode(MapSelectionMode mode);
}
