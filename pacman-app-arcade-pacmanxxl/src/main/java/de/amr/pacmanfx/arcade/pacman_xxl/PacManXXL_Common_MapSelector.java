/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.MapSelectionMode;
import de.amr.pacmanfx.model.MapSelector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.PROPERTY_COLOR_MAP;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static java.util.Objects.requireNonNull;

public class PacManXXL_Common_MapSelector implements MapSelector {

    private final File customMapDir;
    private final ObservableList<WorldMap> customMaps = FXCollections.observableArrayList();
    private final List<WorldMap> builtinMaps = new ArrayList<>();
    private MapSelectionMode selectionMode;

    public PacManXXL_Common_MapSelector(File dir) {
        customMapDir = requireNonNull(dir);
        selectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;
        PacManXXL_Common.addSampleCustomMaps(dir);
    }

    public MapSelectionMode selectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(MapSelectionMode mode) {
        selectionMode = requireNonNull(mode);
    }

    @Override
    public List<WorldMap> builtinMaps() {
        return builtinMaps;
    }

    @Override
    public ObservableList<WorldMap> customMaps() {
        return customMaps;
    }

    @Override
    public void loadCustomMaps() {
        File[] mapFiles = customMapDir.listFiles((dir, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.error("An error occurred accessing custom map directory {}", customMapDir);
            return;
        }
        if (mapFiles.length == 0) {
            Logger.info("No custom maps found");
        } else {
            Logger.info("{} custom map(s) found", mapFiles.length);
        }
        customMaps.clear();
        for (File file : mapFiles) {
            try {
                WorldMap worldMap = WorldMap.loadFromFile(file);
                customMaps.add(worldMap);
                Logger.info("Custom map loaded from file {}", file);
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not read custom map from file {}", file);
            }
        }
    }

    @Override
    public void loadAllMaps() {
        if (builtinMaps.isEmpty()) {
            List<WorldMap> maps = MapSelector.loadMapsFromModule(getClass(), "maps/masonic_%d.world", 8);
            builtinMaps.addAll(maps);
        }
        loadCustomMaps();
    }

    @Override
    public WorldMap getWorldMap(int levelNumber) {
        WorldMap map = switch (selectionMode) {
            case NO_CUSTOM_MAPS -> {
                // first pick built-in maps in order, then randomly
                int index = levelNumber <= builtinMaps.size() ? levelNumber - 1: randomInt(0, builtinMaps.size());
                yield builtinMaps.get(index);
            }
            case CUSTOM_MAPS_FIRST -> {
                if (levelNumber <= customMaps.size()) {
                    // pick custom maps in order
                    yield customMaps.get(levelNumber - 1);
                }
                else {
                    // pick random built-in map
                    yield builtinMaps.get(randomInt(0, builtinMaps().size()));
                }
            }
            case ALL_RANDOM -> {
                int index = randomInt(0, customMaps().size() + builtinMaps().size());
                yield index < customMaps().size() ? customMaps.get(index) : builtinMaps.get(index - customMaps().size());
            }
        };
        WorldMap worldMap = WorldMap.copyOfMap(map);
        // if selected map is a built-in map, use a random color scheme to make it not so boring
        Map<String, String> colorScheme = builtinMaps.contains(map)
            ? PacManXXL_Common.MAP_COLOR_SCHEMES.get(randomInt(0, PacManXXL_Common.MAP_COLOR_SCHEMES.size()))
            : MapSelector.extractColorMap(map);
        worldMap.setConfigValue(PROPERTY_COLOR_MAP, colorScheme);
        Logger.info("Map selected (Mode {}): {}", selectionMode, worldMap.url());

        return worldMap;
    }
}